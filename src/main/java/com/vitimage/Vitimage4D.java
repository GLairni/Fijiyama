package com.vitimage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import com.vitimage.ItkImagePlusInterface.MetricType;
import com.vitimage.ItkImagePlusInterface.OptimizerType;
import com.vitimage.ItkImagePlusInterface.Transformation3DType;
import com.vitimage.TransformUtils.Geometry;
import com.vitimage.TransformUtils.Misalignment;
import com.vitimage.VitimageUtils.Capillary;
import com.vitimage.VitimageUtils.SupervisionLevel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import math3d.Point3d;

public class Vitimage4D implements VitiDialogs,TransformUtils,VitimageUtils{
	public enum VineType{
		GRAFTED_VINE,
		VINE,
		CUTTING
	}
	public final static String slash=File.separator;
	public String title="--";
	public String sourcePath="--";
	public String dataPath="--";
	public SupervisionLevel supervisionLevel=SupervisionLevel.GET_INFORMED;
	public VineType vineType=VineType.CUTTING;
	public final int dayAfterExperience;
	private ArrayList<Acquisition> acquisition;//Observations goes there
	private ArrayList<Geometry> geometry;
	private ArrayList<Misalignment> misalignment;
	private ArrayList<ItkTransform> transformation;
	private ArrayList<Capillary> capillary;
	private int[] referenceImageSize;
	private double[] referenceVoxelSize;
	private AcquisitionType acquisitionStandardReference=AcquisitionType.MRI_T1_SEQ;
	private ImagePlus normalizedHyperImage;
	private ImagePlus mask;
	ImagePlus imageForRegistration;
	private int timeSerieDay=0;
	private String projectName="VITIMAGE";
	private String unit="mm";
	
	/**
	 *  Test sequence for the class
	 */
	public static void main(String[] args) {
		ImageJ ij=new ImageJ();
		Vitimage4D viti = new Vitimage4D(VineType.CUTTING,0,"/home/fernandr/Bureau/Test/VITIMAGE4D");			
		System.out.println("Toto 1");
		viti.start();
		viti.normalizedHyperImage.show();
	}


	
	/**
	 * Top level functions
	 */
	public void start() {
		quickStartFromFile();
		while(nextStep());
	}
	
	public boolean nextStep(){
		int a=readStep();
		switch(a) {
		case -1:
			IJ.showMessage("Critical fail, no directory found Source_data in the current directory");
			return false;
		case 0: // rien --> exit
			if(this.supervisionLevel != SupervisionLevel.AUTONOMOUS)IJ.log("No data in this directory");
			return false;
		case 1://data are read. Time to compute individual calculus for each acquisition
			for (Acquisition acq : this.acquisition) {System.out.println("");acq.start();}
			this.setImageForRegistration();
			this.writeImageForRegistration();
			this.writeParametersToHardDisk();
			break;
		case 2: //individual computations are done. Time to register acquisitions
			this.handleMajorMisalignments();
			this.writeTransforms();
			break;
		case 3: //individual computations are done. Time to register acquisitions
			this.centerAxes();
			this.writeTransforms();
			break;
		case 4: //individual computations are done. Time to register acquisitions
			if(vineType==VineType.CUTTING) detectInoculationPoints();
			this.writeTransforms();
			break;
		case 5: //individual computations are done. Time to register acquisitions
			automaticFineRegistration();
			this.writeTransforms();
			break;
		case 6: //data are registered. Time to share data (i.e. masks of non-computable data...)
			//this.computeMask();
			//this.writeMask();
			break;
		case 7: //Data are shared. Time to compute hyperimage
			this.computeNormalizedHyperImage();
			writeHyperImage();
			break;
		case 8:
			System.out.println("Vitimage 4D, Computation finished for "+this.getTitle());
			return false;
		}
		writeStep(a+1);	
		return true;
	}
	

	public void writeStep(int st) {
		File f = new File(this.getSourcePath(),"STEPS_DONE.tag");
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			out.write("Step="+(st)+"\n");
			out.write("# Last execution time : "+(new Date())+"\n");
			out.close();
		} catch (Exception e) {IJ.error("Unable to write transformation to file: "+f.getAbsolutePath()+"error: "+e);}
	}
	
	public int readStep() {
		String strFile="";
		String line;
		File f = new File(this.getSourcePath(),"STEPS_DONE.tag");
		try {
			BufferedReader in=new BufferedReader(new FileReader(this.getSourcePath()+slash+"STEPS_DONE.tag"));
			while ((line = in.readLine()) != null) {
				strFile+=line+"\n";
			}
        } catch (IOException ex) { ex.printStackTrace();  strFile="None\nNone";        }	
		String[]strLines=strFile.split("\n");
		String st=strLines[0].split("=")[1];
		int a=Integer.valueOf(st);
		return(a);		
	}


	public void readProcessedImages(int step){
		if(step <1) IJ.log("Error : read process images, but step is lower than 1");
//		if(step>=2) {for (Acquisition acq : this.acquisition)acq.start();readImageForRegistration();}
		if(step>=3)readTransforms();
		if(step>=4) readMask();
		if(step>=5) readHyperImage();
	}
	
	
	public Vitimage4D(VineType vineType, int dayAfterExperience,String sourcePath) {
		this.sourcePath=sourcePath;
		this.dayAfterExperience=dayAfterExperience;
		this.vineType=vineType;
		acquisition=new ArrayList<Acquisition>();//Observations goes there
		geometry=new ArrayList<Geometry>();
		misalignment=new ArrayList<Misalignment>();
		capillary=new ArrayList<Capillary> ();
		imageForRegistration=null;
		transformation=new ArrayList<ItkTransform>();
		transformation.add(new ItkTransform());//No transformation for the first image, that is the reference image
	}
	
	
	
	/**
	 * Medium level functions
	 */
	public void quickStartFromFile() {
		//Acquisitions auto-detect
		//Detect T1 sequence
		//Gather the path to source data
		System.out.println("Looking for a Vitimage4D hosted in "+this.sourcePath);

		//Explore the path, look for STEPS_DONE.tag and DATA_PARAMETERS.tag
		File fStep=new File(this.sourcePath+slash+"STEPS_DONE.tag");
		File fParam=new File(this.sourcePath+slash+"DATA_PARAMETERS.tag");

		if(fStep.exists() && fParam.exists() ) {
			//read the actual step use it to open in memory all necessary datas			
			System.out.println("It's a match ! The tag files tells me that data have already been processed here");
			System.out.println("Start reading acquisitions");
			readAcquisitions();			
			for (Acquisition acq : this.acquisition) {System.out.println("");acq.start();}
			readParametersFromHardDisk();//Read parameters, path and load data in memory
			this.setImageForRegistration();
			readParametersFromHardDisk();//Read parameters, path and load data in memory
			writeParametersToHardDisk();//In the case that a data appears since last time
			System.out.println("Start reading parameters");
			System.out.println("Start reading processed images");
			this.readProcessedImages(readStep());
			System.out.println("Reading done...");
		}
		else {		
			//look for a directory Source_data
			System.out.println("No match with previous analysis ! Starting new analysis...");
			File directory = new File(this.sourcePath,"Source_data");
			if(! directory.exists()) {
				writeStep(-1);
				return;
			}
			File dir = new File(this.sourcePath+slash+"Computed_data");
			dir.mkdirs();
			writeStep(0);
			System.out.println("Exploring Source_data...");
			readAcquisitions();
			System.out.println("Writing parameters file");
			writeStep(1);
		}
	}
		
	public void readAcquisitions() {
		File dataSourceDir = new File(this.sourcePath,"Source_data");
		//Lire le contenu, et chercher des dossiers RX MRI_T1_SEQ MRI_T2_SEQ
		File t1SourceDir=new File(this.sourcePath+slash+"Source_data"+slash+"MRI_T1_SEQ");
		if(t1SourceDir.exists()) {
			//A ajouter : extraire des données la geometrie generale
			this.addAcquisition(AcquisitionType.MRI_T1_SEQ,this.sourcePath+slash+"Source_data"+slash+"MRI_T1_SEQ",
					Geometry.REFERENCE,
					Misalignment.LIGHT_RIGID,Capillary.HAS_CAPILLARY,this.supervisionLevel);
		}
		
		
		File t2SourceDir=new File(this.sourcePath+slash+"Source_data"+slash+"MRI_T2_SEQ");
		if(t2SourceDir.exists()) {
			//A ajouter : extraire des données la geometrie generale
			this.addAcquisition(AcquisitionType.MRI_T2_SEQ,this.sourcePath+slash+"Source_data"+slash+"MRI_T2_SEQ",
					Geometry.QUASI_REFERENCE,
					Misalignment.LIGHT_RIGID,Capillary.HAS_CAPILLARY,this.supervisionLevel);
		}

		
		File rxSourceDir=new File(this.sourcePath+slash+"Source_data"+slash+"RX");
		if(rxSourceDir.exists()) {
			//A ajouter : extraire des données la geometrie generale
			this.addAcquisition(AcquisitionType.RX,this.sourcePath+slash+"Source_data"+slash+"RX",
					Geometry.QUASI_REFERENCE,
					Misalignment.LIGHT_RIGID,Capillary.HAS_NO_CAPILLARY,this.supervisionLevel);
		}
		System.out.println("Number of acquisitions detected : "+acquisition.size());
			
	}
	
	public void addAcquisition(Acquisition.AcquisitionType acq, String path,Geometry geom,Misalignment mis,Capillary cap,SupervisionLevel sup){
		
		switch(acq) {
		case MRI_T1_SEQ: this.acquisition.add(new MRI_T1_Seq(path,cap,sup));break;
		case MRI_T2_SEQ: this.acquisition.add(new MRI_T2_Seq(path,cap,sup));break;
		case MRI_DIFF_SEQ: VitiDialogs.notYet("FlipFlop");break;
		case MRI_FLIPFLOP_SEQ: VitiDialogs.notYet("FlipFlop");break;
		case MRI_SE_SEQ: VitiDialogs.notYet("FlipFlop");break;
		case MRI_GE3D_SEQ: VitiDialogs.notYet("FlipFlop");break;
		case RX: this.acquisition.add(new RX(path,cap,sup));break;
		case HISTOLOGY: VitiDialogs.notYet("FlipFlop");break;
		case PHOTOGRAPH: VitiDialogs.notYet("FlipFlop");break;
		case TERAHERTZ: VitiDialogs.notYet("FlipFlop");break;
		}
		this.geometry.add(geom);
		this.misalignment.add(mis);
		this.capillary.add(cap);
		int indexCur=this.acquisition.size()-1;
		if(geom==Geometry.REFERENCE) {
			this.referenceImageSize=new int[] {this.acquisition.get(indexCur).dimX(),
					this.acquisition.get(indexCur).dimY(),
					this.acquisition.get(indexCur).dimZ()};
			this.referenceVoxelSize=new double[] {this.acquisition.get(indexCur).voxSX(),
					this.acquisition.get(indexCur).voxSY(),
					this.acquisition.get(indexCur).voxSZ()};
		}
		this.transformation.add(new ItkTransform());
	}
	


	public void readParametersFromHardDisk() {
		File fParam=new File(this.sourcePath,"DATA_PARAMETERS.tag");
		String[]strFile=null;
		try {
			 String str= Files.lines(Paths.get(fParam.getAbsolutePath()) ).collect(Collectors.joining("\n"));
			 strFile=str.split("\n");
       } catch (IOException ex) {        ex.printStackTrace();   }
		for(int i=1;i<strFile.length ; i++) {
			strFile[i]=strFile[i].split("=")[1];
		}
		this.sourcePath=strFile[2];
		this.dataPath=strFile[3];
		this.setTitle(strFile[4]);
		this.timeSerieDay=Integer.valueOf(strFile[5]);
		this.projectName=strFile[6];
		this.vineType=VitimageUtils.stringToVineType(strFile[7]);
		this.setDimensions(Integer.valueOf(strFile[9]) , Integer.valueOf(strFile[10]) , Integer.valueOf(strFile[11]) );
		this.unit=strFile[12];
		this.setVoxelSizes(Double.valueOf(strFile[13]), Double.valueOf(strFile[14]), Double.valueOf(strFile[15]));
	}
	
	public void writeParametersToHardDisk() {
		System.out.println("Here is the writing of parameters, with dimX="+this.dimX());
		File fParam=new File(this.sourcePath,"DATA_PARAMETERS.tag");
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fParam)));
			out.write("############# DATA PARAMETERS TAG FILE ###########\n");
			out.write("DayOfWritingTagFile="+(new Date()) +"\n");
			out.write("LocalSourcePath="+this.sourcePath+"\n");
			out.write("LocalDataPath="+this.dataPath+"\n");
			out.write("Title="+this.getTitle()+"\n");
			out.write("TimeSerieDay="+this.timeSerieDay+"\n");
			out.write("Project="+this.projectName +"\n");
			out.write("VineType="+ this.vineType+"\n");
			out.write("NumberOfAcquisitions="+this.acquisition.size()+"\n");
			out.write("DimX(pix)="+this.dimX() +"\n");
			out.write("DimY(pix)="+this.dimY() +"\n");
			out.write("DimZ(pix)="+this.dimZ()+"\n" );
			out.write("Unit="+this.unit +"\n");
			out.write("VoxSX="+this.voxSX() +"\n");
			out.write("VoxSY="+this.voxSY() +"\n");
			out.write("VoxSZ="+this.voxSZ() +"\n");
			out.close();
		} catch (Exception e) {IJ.error("Unable to write transformation to file: "+fParam.getAbsolutePath()+"error: "+e);}	
	}
	
	
	public void setDimensions(int dimX,int dimY,int dimZ) {
		this.referenceImageSize=new int[] {dimX,dimY,dimZ};
	}

	public void setVoxelSizes(double voxSX,double voxSY,double voxSZ) {
		this.referenceVoxelSize=new double[] {voxSX,voxSY,voxSZ};
	}

	public void setImageForRegistration() {
		this.imageForRegistration=new Duplicator().run(this.acquisition.get(0).getImageForRegistration());
		this.setDimensions(this.imageForRegistration.getWidth(), this.imageForRegistration.getHeight(), this.imageForRegistration.getStackSize());
		this.setVoxelSizes(this.imageForRegistration.getCalibration().pixelWidth, this.imageForRegistration.getCalibration().pixelHeight, this.imageForRegistration.getCalibration().pixelDepth);
	}
	

	public void readImageForRegistration() {
		this.imageForRegistration=IJ.openImage(this.sourcePath+slash+ "Computed_data"+slash+"0_Registration"+slash+"imageForRegistration.tif");
		this.setDimensions(this.imageForRegistration.getWidth(), this.imageForRegistration.getHeight(), this.imageForRegistration.getStackSize());
		this.setVoxelSizes(this.imageForRegistration.getCalibration().pixelWidth, this.imageForRegistration.getCalibration().pixelHeight, this.imageForRegistration.getCalibration().pixelDepth);
	}
	
	public void writeImageForRegistration() {
		File dir = new File(this.sourcePath+slash+"Computed_data"+slash+"0_Registration");
		dir.mkdirs();
		IJ.saveAsTiff(this.imageForRegistration,this.sourcePath+slash+ "Computed_data"+slash+"0_Registration"+slash+"imageForRegistration.tif");
	}

	
	public void readHyperImage() {
		this.normalizedHyperImage =IJ.openImage(this.sourcePath+slash+ "Computed_data"+slash+"2_HyperImage"+slash+"hyperImage.tif");
	}
	
	public void writeHyperImage() {
		File dir = new File(this.sourcePath+slash+"Computed_data"+slash+"2_HyperImage");
		dir.mkdirs();
		IJ.saveAsTiff(this.normalizedHyperImage,this.sourcePath+slash+ "Computed_data"+slash+"2_HyperImage"+slash+"hyperImage.tif");
	}

	public void readMask() {
		this.mask=IJ.openImage(this.sourcePath + slash + "Computed_data" + slash + "0_Mask" + slash + "mask.tif");
	}
	
	public void writeMask() {
		File dir = new File(this.sourcePath+slash+"Computed_data"+slash+"1_Mask");
		dir.mkdirs();
		IJ.saveAsTiff(this.mask,this.sourcePath + slash + "Computed_data" + slash + "1_Mask" + slash + "mask.tif");
	}

	public void writeTransforms() {
		File dir = new File(this.sourcePath+slash+"Computed_data"+slash+"0_Registration");
		dir.mkdirs();
		for(int i=0;i<this.transformation.size() ; i++) {
			this.transformation.get(i).writeToFile(this.sourcePath+slash+ "Computed_data"+slash+"0_Registration"+slash+"transformation_"+i+".txt");
		}
	}

	public void writeRegisteringTransforms(String registrationStep) {
		File dir = new File(this.sourcePath+slash+"Computed_data"+slash+"0_Registration");
		dir.mkdirs();
		for(int i=0;i<this.transformation.size() ; i++) {
			this.transformation.get(i).writeToFile(this.sourcePath+slash+ "Computed_data"+slash+"0_Registration"+slash+"transformation_"+i+"_step_"+registrationStep+".txt");
		}
	}
	

	public void readTransforms() {
		for(int i=0;i<this.transformation.size() ; i++) {
			this.transformation.set(i,ItkTransform.readFromFile(this.sourcePath+slash+ "Computed_data"+slash+"0_Registration"+slash+"transformation_"+i+".txt"));
		}
	}

	public void computeMask() {
		//VitiDialogs.notYet("Compute Mask in Vitimage4D");
	}
	

	
	
	
	
	public void handleMajorMisalignments() {
		for(int i=0;i<acquisition.size();i++) {
			if(geometry.get(i)==Geometry.REFERENCE) continue;
			if(geometry.get(i)==Geometry.UNKNOWN) {}
			switch(geometry.get(i)){
			case REFERENCE: 	
				break;
			case UNKNOWN: 
				VitiDialogs.notYet("Geometry.UNKNOWN");
				break;
			case MIRROR_X:
				VitiDialogs.notYet("Geometry.MIRROR_X");
				break;
			case MIRROR_Y:
				VitiDialogs.notYet("Geometry.MIRROR_Y");
				break;
			case MIRROR_Z:
				VitiDialogs.notYet("Geometry.MIRROR_Z");
				break;
			case SWITCH_XY:
				VitiDialogs.notYet("Geometry.SWITCH_XY");
				break;
			case SWITCH_XZ:
				VitiDialogs.notYet("Geometry.SWITCH_XZ");
				break;
			case SWITCH_YZ:
				VitiDialogs.notYet("Geometry.SWITCH_YZ");
				break;
			}			
		}
	}
	
	public void centerAxes() {
		double refCenterX=acquisition.get(0).dimX()*acquisition.get(0).voxSX()/2.0;
		double refCenterY=acquisition.get(0).dimY()*acquisition.get(0).voxSY()/2.0;
		double refCenterZ=acquisition.get(0).dimZ()*acquisition.get(0).voxSZ()/2.0;
		for(int i=0;i<acquisition.size();i++) {
			Acquisition acq=acquisition.get(i);
			acq.imageForRegistration.setTitle("i="+i+" before all");
			Point3d[]pInit=new Point3d[3];
			Point3d[]pFin=new Point3d[3];
			pFin=VitimageUtils.detectAxis(acq);//in the order : center of object along its axis , center + daxis , center + dvect Orthogonal to axis 				
			pInit[0]=new Point3d(refCenterX, refCenterY     , refCenterZ     );//origine
			pInit[1]=new Point3d(refCenterX, refCenterY     , 1 + refCenterZ );//origine + dZ
			pInit[2]=new Point3d(refCenterX, 1 + refCenterY , refCenterZ     );//origine + dY
			System.out.println("\nCentrage axe image. Depart");
			System.out.println("Points prevus : \nPinit0="+pInit[0]);
			System.out.println("Pinit1="+pInit[1]);
			System.out.println("Pinit2="+pInit[2]);
			System.out.println("\nDetect axis : \nPFin0="+pFin[0]);
			System.out.println("PFin1="+pFin[1]);
			System.out.println("PFin2="+pFin[2]);
			ItkTransform trAdd=ItkTransform.estimateBestRigid3D(pFin, pInit);
			System.out.println("Transformation calculee =\n"+trAdd);
			transformation.get(i).addTransform(trAdd);
		}
		writeRegisteringTransforms("afterAxisAlignment");
		writeRegisteringImages("afterAxisAlignment");
	}
	
	public void detectInoculationPoints(){
		double refCenterX=acquisition.get(0).dimX()*acquisition.get(0).voxSX()/2.0;
		double refCenterY=acquisition.get(0).dimY()*acquisition.get(0).voxSY()/2.0;
		double refCenterZ=acquisition.get(0).dimZ()*acquisition.get(0).voxSZ()/2.0;
		for(int i=0;i<acquisition.size();i++) {
			Acquisition acq=acquisition.get(i);
			Point3d[]pFin=VitimageUtils.detectInoculationPoint(this.transformation.get(i).transformImage(this.acquisition.get(0).getImageForRegistration(),this.acquisition.get(i).getImageForRegistration()),acq.valMedForThresholding);
			
			//Compute and store the inoculation Point, in the coordinates of the original image
			Point3d inoculationPoint=ItkImagePlusInterface.vectorDoubleToPoint3d(
					acq.getTransform().transformPoint(ItkImagePlusInterface.doubleArrayToVectorDouble(
							new double[] {pFin[3].x,pFin[3].y,pFin[3].z})));
			acq.setInoculationPoint(inoculationPoint);

			
			//Compute the transformation that align the inoculation point to the Y+, with the axis already aligned
			Point3d[]pInit=new Point3d[3];
			pInit[0]=new Point3d( refCenterX  , refCenterY     , refCenterZ      );
			pInit[1]=new Point3d( refCenterX  , refCenterY     , refCenterZ + 1  );
			pInit[2]=new Point3d( refCenterX  , refCenterY + 1 , refCenterZ      );

			Point3d[] pFinTrans=new Point3d[] { pFin[0] , pFin[1] , pFin[2] };
			ItkTransform trAdd=ItkTransform.estimateBestRigid3D(pFinTrans,pInit);
			transformation.get(i).addTransform(trAdd);
			//acq.setImageForRegistration();				
		}
		writeRegisteringTransforms("afterIPalignment");
		writeRegisteringImages("afterIPalignment");	
	}
	
	public void automaticFineRegistration() {
		ImagePlus imgDebug=this.acquisition.get(0).getImageForRegistrationWithoutCapillary();
		imgDebug.getProcessor().resetMinAndMax();
		VitimageUtils.imageChecking(imgDebug);

		System.out.println("La transformation qui va etre appliquee est la suivante "+this.transformation.get(0));
		ImagePlus imgRef= this.transformation.get(0).transformImage(this.acquisition.get(0).imageForRegistration,this.acquisition.get(0).getImageForRegistrationWithoutCapillary());
		imgRef.getProcessor().resetMinAndMax();
		VitimageUtils.imageChecking(imgRef);
		for (int i=0;i<this.acquisition.size();i++) {
			if(i==0)continue;
			ItkRegistrationManager manager=new ItkRegistrationManager();
			
			ImagePlus img=this.acquisition.get(i).getImageForRegistrationWithoutCapillary();
			System.out.println("Image checking de get image without capillary");
			img.getProcessor().resetMinAndMax();
			VitimageUtils.imageChecking(img);
			img= this.transformation.get(i).transformImage(this.acquisition.get(0).imageForRegistration,this.acquisition.get(i).getImageForRegistrationWithoutCapillary());
			System.out.println("Image checking de get image without capillary transformed");
			img.getProcessor().resetMinAndMax();
			VitimageUtils.imageChecking(img);
			System.out.println(this.transformation.get(i));
			this.transformation.get(i).addTransform(manager.runScenarioInterModal(new ItkTransform(),imgRef,img));
			System.out.println(this.transformation.get(i));
			this.transformation.set(i,this.transformation.get(i).simplify());
		}
		writeRegisteringImages("afterItkRegistration");	
		writeRegisteringTransforms("afterItkRegistration");
	}
	
	
	public void writeRegisteringImages(String registrationStep) {
		for(int i=0;i<this.acquisition.size() ; i++) {
			ImagePlus tempView=this.transformation.get(i).transformImage(this.acquisition.get(0).getImageForRegistration(),this.acquisition.get(i).getImageForRegistration());
			tempView.getProcessor().resetMinAndMax();
			IJ.saveAsTiff(tempView, this.sourcePath+slash+"Computed_data"+slash+"0_Registration"+slash+"imgRegistration_acq_"+i+"_step_"+registrationStep+".tif");
		}
	}
	
	public void computeNormalizedHyperImage() {
		
		ArrayList<ImagePlus> imgList=new ArrayList<ImagePlus>();
		ImagePlus[] hyp;
		ImagePlus []hypRX=VitimageUtils.stacksFromHyperstack(acquisition.get(2).normalizedHyperImage,1);
		ImagePlus []hypT1=VitimageUtils.stacksFromHyperstack(acquisition.get(0).normalizedHyperImage,2);
		
		for(int i=0;i<acquisition.size();i++) {
			Acquisition acq=acquisition.get(i);
			hyp=VitimageUtils.stacksFromHyperstack(acq.normalizedHyperImage,acq.hyperSize);
			switch(acq.acquisitionType) {
			case RX:imgList.add( transformation.get(i).transformImage( acquisition.get(0).imageForRegistration ,hyp[0]));break;
			case MRI_T1_SEQ:imgList.add( transformation.get(i).transformImage( acquisition.get(0).imageForRegistration ,hyp[0]));
							imgList.add( transformation.get(i).transformImage( acquisition.get(0).imageForRegistration ,hyp[1]));break;
			case MRI_T2_SEQ:imgList.add( transformation.get(i).transformImage( acquisition.get(0).imageForRegistration ,hyp[1]));break;			
			}
		}
		ImagePlus[]tabRet=new ImagePlus[imgList.size()];
		for(int i=0;i<imgList.size() ;i++) tabRet[i]=imgList.get(i);
		this.normalizedHyperImage=Concatenator.run(tabRet);
	}

	
	
	
	/**
	 * Minor functions
	 */
	public String getSourcePath() {
		return this.sourcePath;
	}
	
	public void setSourcePath(String path) {
		this.sourcePath=path;
	}

	public String getTitle(){
		return title;
	}
	
	public void setTitle(String title){
		this.title=title;
	}
	

	public int dimX() {
		return this.referenceImageSize==null ? this.acquisition.get(0).dimX() : this.referenceImageSize[0];
	}

	public int dimY() {
		return this.referenceImageSize==null ? this.acquisition.get(0).dimY() : this.referenceImageSize[1];
	}

	public int dimZ() {
		return this.referenceImageSize==null ? this.acquisition.get(0).dimZ() : this.referenceImageSize[2];
	}
	
	public double voxSX() {
		return this.referenceVoxelSize==null ? this.acquisition.get(0).voxSX() : this.referenceVoxelSize[0];
	}

	public double voxSY() {
		return this.referenceVoxelSize==null ? this.acquisition.get(0).voxSY() : this.referenceVoxelSize[1];
	}

	public double voxSZ() {
		return this.referenceVoxelSize==null ? this.acquisition.get(0).voxSZ() : this.referenceVoxelSize[2];
	}
	
	
}