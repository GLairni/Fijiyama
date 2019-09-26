package com.vitimage;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Path;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.itk.simple.DisplacementFieldTransform;
import org.itk.simple.Image;
import org.itk.simple.ImageFileWriter;
import org.itk.simple.InverseDisplacementFieldImageFilter;
import org.itk.simple.ResampleImageFilter;
import org.itk.simple.Transform;

import com.vitimage.ItkImagePlusInterface.MetricType;
import com.vitimage.ItkImagePlusInterface.Transformation3DType;
import com.vitimage.Vitimage4D.VineType;
import com.vitimage.VitimageUtils.AcquisitionType;
import com.vitimage.VitimageUtils.Capillary;
import com.vitimage.VitimageUtils.ComputingType;
import com.vitimage.VitimageUtils.SupervisionLevel;

import Hough_Package.Hough_Circle;
import distance.Correlation;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.ChannelSplitter;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.FolderOpener;
import ij.plugin.HyperStackConverter;
import ij.plugin.ImageCalculator;
import ij.plugin.RGBStackMerge;
import ij.plugin.StackCombiner;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.FloatPolygon;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel3D;
import math3d.Point3d;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.distanceMap3d.EDT;
import ij.measure.ResultsTable;
import trainableSegmentation.Trainable_Segmentation;

public class TestRomain {

	
	public static void main(String[]args) {
		ImageJ ij=new ImageJ();
//		produceTestForSplineField();
		//testFields();
//		produceIntermediary();
		//		testSpline0();
		//test3Fields();
		//tresGrosseChiasse();
//		petitCacaToutMou();
		//chiachia();
//		scheisse();
		//makeAllGeImport();
		//makeAllAssistedDetections();
		//finishAll4D();
//		makeAll5D();
//		makeAllTiImport();
		//testSuccessiveRegistration();		//enormeChiasse();
		//produceStackTestForPhoto();
		//importOnlyGe3dForVerif();
		//testRandom();
		//testCon();
//		testCorr();
		//chiasse5();
//		chiasse7();
		//testBM();
		//makeAllPhoto();
//		inspectAllPhoto();
		//publishResults();
		//testHoughGameTransform();
		//makeTrainingHybride();
		//		testBalanceDesBlancs();
		//inspectRXTof();
		//viewMRI();

	//	takeInputCorrespondanceForBiasEstimation();
	//	computeFieldBiasBasedOnMultipleCorrespondances(0,true,false);
		//chia(0,true,false);


		//computeFieldBiasBasedOnMultipleCorrespondances(0,false,false);
//		chia(0,false,false);
		//testStrategyField();
		/*
		boolean fixCenter=true;
		boolean gauss=true;
		double sigma=10;
		computeFieldBiasBasedOnMultipleCorrespondances(0,fixCenter,gauss,sigma);
		chia(0,fixCenter,gauss,sigma);
		sigma=15;
		computeFieldBiasBasedOnMultipleCorrespondances(0,fixCenter,gauss,sigma);
		chia(0,fixCenter,gauss,sigma);
*/
		//computeFieldBiasBasedOnMultipleCorrespondances();//takeInputCorrespondanceForBiasEstimation();
//		computeImageForClinicalMRIBiasEstimation();
		//pequenaJala();
//		testTransT1T2();
//		testTransT1M0();
		makeHybrideCepVertical();
//		testTrans();
//		//		charlotte();
		System.out.println("FINI !");
		VitimageUtils.waitFor(100000000);
		System.exit(0);
	}
	
	
	public static ImagePlus makeCombination2by3(ImagePlus[]tab) {
		StackCombiner sc=new StackCombiner();
		ImageStack l1=sc.combineHorizontally(sc.combineHorizontally(tab[0].getStack(),tab[1].getStack()),tab[2].getStack());
		ImageStack l2=sc.combineHorizontally(sc.combineHorizontally(tab[3].getStack(),tab[4].getStack()),tab[5].getStack());
		ImagePlus ret=new ImagePlus("",sc.combineVertically(l1, l2));
		VitimageUtils.adjustImageCalibration(ret,tab[0]);
		return ret;
	}

	public static ImagePlus makeCombination2by3(ImagePlus i1,ImagePlus i2,ImagePlus i3,ImagePlus i4,ImagePlus i5,ImagePlus i6) {
		return makeCombination2by3(new ImagePlus[] {i1,i2,i3,i4,i5,i6});
	}

	
	
	
	public static void makeHybrideCepVertical() {
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		String []modalities= {"Mod_0_low_photo_bnw.tif", "Mod_1_low_rx.tif","Mod_2_low_mri_t1.tif","Mod_3_low_mri_t2.tif","Mod_4_low_mri_m0.tif","Mod_5_low_mri_diff.tif","Mod_6_low_photo_Red.tif","Mod_7_low_photo_Green.tif","Mod_8_low_photo_Blue.tif"};
		int []slices=new int[] {5,10,15,20,25,30,35,40,45,50,60,70,80,90,100,110};
		ImagePlus[][]tabMods=new ImagePlus[12][9];
		ImagePlus[]tabFinal=new ImagePlus[9];
		ImagePlus[][][]tabSlices=new ImagePlus[12][9][slices.length];
		ImagePlus[][]tabSlicesVert=new ImagePlus[9][slices.length*12];
		ImagePlus[][]tabSlicesHoriz=new ImagePlus[9][slices.length*2 ];
		for(int i=0;i<specimen.length;i++) {
			System.out.println("Lecture "+specimen[i]);
			String rep="/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Source_data/";
			for(int j=0;j<modalities.length;j++) {
				tabMods[i][j]=IJ.openImage(rep+"/PHOTOGRAPH/Computed_data/3_Hyperimage/"+modalities[j]);
				for(int k=0;k<slices.length;k++) {
					tabMods[i][j].setSlice(slices[k]+1);
					tabSlices[i][j][k]=tabMods[i][j].crop();
					tabSlices[i][j][k]=VitimageUtils.writeTextOnImage(specimen[i]+" "+modalities[j]+" z="+(slices[k]+1), tabSlices[i][j][k],11,0);
				}
			}
		}
	
		System.out.println("Construction verticale ");
		//Construction vert
		for(int mod=0;mod<9;mod++) {
			System.out.print(" "+mod);
			for(int slHyb=0;slHyb<12*slices.length;slHyb++) {
				int slSpec=slHyb%12;
				int slProf=slHyb/12;
				tabSlicesVert[mod][slHyb]=VitimageUtils.imageCopy(tabSlices[slSpec][mod][slProf]);
			}		
			tabFinal[mod]=Concatenator.run(tabSlicesVert[mod]);			
		}	
		System.out.println("\nHyperImage");
		ImagePlus retVert=Concatenator.run(tabFinal);
		IJ.run(retVert,"Stack to Hyperstack...", "order=xyczt(default) channels=1 slices="+(12*slices.length)+" frames=9 display=Grayscale");
		retVert.show();


		System.out.println("Construction horizontale ");
		//Construction horiz
		for(int mod=0;mod<9;mod++) {
			System.out.print(" "+mod);
			for(int slHyb=0;slHyb<slices.length;slHyb++) {
				tabSlicesHoriz[mod][slHyb*2]=makeCombination2by3(   
					tabSlices[0][mod][slHyb], tabSlices[2][mod][slHyb] , tabSlices[4][mod][slHyb] , tabSlices[6][mod][slHyb] ,tabSlices[8][mod][slHyb], tabSlices[10][mod][slHyb]);
				tabSlicesHoriz[mod][slHyb*2+1]=makeCombination2by3(   
					tabSlices[1][mod][slHyb], tabSlices[3][mod][slHyb] , tabSlices[5][mod][slHyb] , tabSlices[7][mod][slHyb] ,tabSlices[9][mod][slHyb], tabSlices[11][mod][slHyb]);
			}		
			tabFinal[mod]=Concatenator.run(tabSlicesHoriz[mod]);			
		}
		System.out.println("\nHyperImage");
		ImagePlus retHoriz=Concatenator.run(tabFinal);
		IJ.run(retHoriz,"Stack to Hyperstack...", "order=xyczt(default) channels=1 slices="+(2*slices.length)+" frames=9 display=Grayscale");
		retHoriz.show();
	}
	
	public static void tt() {
		String rep="/home/fernandr/Bureau/Traitements/Cep5D/CEP013_AS3/Source_data/PHOTOGRAPH/Computed_data/3_Hyperimage/";
		ImagePlus t1=IJ.openImage(rep+"Mod_1_low_rx.tif");
		ImagePlus t2=IJ.openImage(rep+"Mod_2_low_mri_t1.tif");

		t1.show();
		t1.setTitle("t1");
		t1.setSlice(100);
		t2.show();
		t2.setSlice(100);
		t2.setTitle("t2");
		System.out.println("3");
		VitimageUtils.waitFor(1000);
		System.out.println("2");
		VitimageUtils.waitFor(1000);
		System.out.println("1");
		VitimageUtils.waitFor(1000);
		VitimageUtils.actualizeData(t2, t1);
		System.out.println("Lapin !");
		VitimageUtils.waitFor(1000);
		System.out.println("En effet :");
/*		ImagePlus t3=VitimageUtils.imageCopy(t1);
		ImagePlus t4=VitimageUtils.imageCopy(t2);

		t3.show();
		t3.setSlice(100);
		t3.setTitle("t3");
		t4.show();
		t4.setSlice(100);
		t4.setTitle("t4");
		*/
	}
	
	public static void testT2M0() {
		String rep="/home/fernandr/Bureau/Traitements/Cep5D/CEP013_AS3/Source_data/PHOTOGRAPH/Computed_data/3_Hyperimage/";
		int chiffre=40;
		VitimageUtils.compositeGridByte(IJ.openImage(rep+"Mod_1_low_rx.tif"),IJ.openImage(rep+"Mod_2_low_mri_t1.tif"),chiffre,chiffre,chiffre,"T1RX avant").show();		
	}
	
	
	
	public static void testTransT1T2() {
		String rep="/home/fernandr/Bureau/Test/T12/";
		ImagePlus imgT1=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/CEP011_AS1/Source_data/MRI_CLINICAL/Computed_data/3_HyperImage/stack_0.tif");
		ImagePlus imgT2=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/CEP011_AS1/Source_data/MRI_CLINICAL/Computed_data/3_HyperImage/stack_1.tif");
		ItkTransform trT2=MRI_Clinical.registerT1T2(imgT1,imgT2,null,2); 
		ImagePlus imgT2Reg=trT2.transformImage(imgT1, imgT2);
		IJ.saveAsTiff(imgT1, rep+"T1.tif");
		IJ.saveAsTiff(imgT2, rep+"T2.tif");
		IJ.saveAsTiff(imgT2Reg, rep+"T2REC.tif");		
	}
	public static void testTransT1M0() {
		String rep="/home/fernandr/Bureau/Test/T12/";
		ImagePlus imgT1=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/CEP011_AS1/Source_data/MRI_CLINICAL/Computed_data/3_HyperImage/stack_0.tif");
		ImagePlus imgM0=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/CEP011_AS1/Source_data/MRI_CLINICAL/Computed_data/3_HyperImage/stack_2.tif");
		ItkTransform trM0=MRI_Clinical.registerT1T2(imgT1,imgM0,null,2); 
		ImagePlus imgM0Reg=trM0.transformImage(imgT1, imgM0);
		IJ.saveAsTiff(imgT1, rep+"T1.tif");
		IJ.saveAsTiff(imgM0, rep+"M0.tif");
		IJ.saveAsTiff(imgM0Reg, rep+"M0REC.tif");
	}
	
	public static void testTrans() {
		String []specs=new String[] {"CEP011_AS1","CEP012_AS2"};
		for(String spec : specs) {
			ImagePlus imgMov=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/"+spec+"/Source_data/PHOTOGRAPH/Computed_data/3_Hyperimage/Mod_2_low_mri_t1.tif");
			ImagePlus imgRef=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/"+spec+"/Source_data/PHOTOGRAPH/Computed_data/3_Hyperimage/Mod_1_low_rx.tif");
			ImagePlus imgMovSub=VitimageUtils.Sub222(imgMov);
			ImagePlus imgRefSub=VitimageUtils.Sub222(imgRef);
			double sigma=15;
			int levelMax=2;
			int levelMin=1;
			
			int viewSlice=18;
			int nbIterations=10;
			int neighXY=2;	int neighZ=0;
			int bSXY=5;		int bSZ=2;
			int strideXY=1;		int strideZ=1;
			ItkTransform transRet=null;
	
	
			sigma=10;
			levelMax=1;
			levelMin=1;
			BlockMatchingRegistration bmRegistration3=new BlockMatchingRegistration(imgRefSub,imgMovSub,Transformation3DType.DENSE,MetricType.CORRELATION,
					0,sigma,levelMin,levelMax,nbIterations,viewSlice,null,neighXY,neighZ,bSXY,bSZ,strideXY,strideZ);
			bmRegistration3.displayRegistration=false;
			bmRegistration3.displayR2=false;
			transRet=bmRegistration3.runMultiThreaded(null);
			//bmRegistration.closeLastImages();
			bmRegistration3.freeMemory();
			ImagePlus res3=transRet.transformImage(imgRef, imgMov);
			res3.setDisplayRange(0, 255);
			IJ.run(res3,"8-bit","");
			res3.show();
			res3.setTitle("res3");
			ImagePlus comp3=VitimageUtils.compositeOf(imgRef, res3);
			IJ.saveAsTiff(comp3, "/home/fernandr/Bureau/Test/TestRegClin/"+spec+"comp3_SIG_10_CORR.tif");
			transRet=transRet.flattenDenseField(imgRef);
			transRet.writeAsDenseField("/home/fernandr/Bureau/Test/TestRegClin/"+spec+"field3_10_CORR.tif", imgRef);
	
			sigma=15;
			levelMax=1;
			levelMin=1;
			bmRegistration3=new BlockMatchingRegistration(imgRef,imgMov,Transformation3DType.DENSE,MetricType.SQUARED_CORRELATION,
					0,sigma,levelMin,levelMax,nbIterations,viewSlice,null,neighXY,neighZ,bSXY,bSZ,strideXY,strideZ);
			bmRegistration3.displayRegistration=false;
			bmRegistration3.displayR2=false;
			transRet=bmRegistration3.runMultiThreaded(null);
			//bmRegistration.closeLastImages();
			bmRegistration3.freeMemory();
			res3=transRet.transformImage(imgRef, imgMov);
			res3.setDisplayRange(0, 255);
			IJ.run(res3,"8-bit","");
			res3.show();
			res3.setTitle("res3");
			comp3=VitimageUtils.compositeOf(imgRef, res3);
			IJ.saveAsTiff(comp3, "/home/fernandr/Bureau/Test/TestRegClin/"+spec+"comp3_SIG_15_CORR.tif");
			transRet=transRet.flattenDenseField(imgRef);
			transRet.writeAsDenseField("/home/fernandr/Bureau/Test/TestRegClin/"+spec+"field3_15_CORR.tif", imgRef);
		}
	}
			
	
	
	public static void charlotte() {
		System.out.println(true);
	}
	
	public static void chi() {
		ItkTransform trans=ItkTransform.readAsDenseField("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/champ_0_FIX.tif");
		ImagePlus imgRef=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/compAvant_CEP011_AS1.tif");
		trans.showAsGrid3D(imgRef, 20, "Field", 200);		
		trans=ItkTransform.smoothDeformationTransform(trans,5,5,5);
		trans.showAsGrid3D(imgRef, 20, "Field Gauss", 200);		
		
	}
	
	public static void chia(int it,boolean fixCenter,boolean gauss,double sigma) {
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		ItkTransform trans=ItkTransform.readAsDenseField("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/champ_"+it+("_SIG"+sigma)+(fixCenter ? "_FIX" : "_NO")+(gauss ? "_GAUSS" : "")+".tif");
		for(String spec : specimen) {
			System.out.println("Traitement "+spec);
			System.out.println("chargement images");
			ImagePlus mri=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/mri_"+spec+".tif");
			ImagePlus rx=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/rx_"+spec+".tif");
			System.out.println("transformation image");
			ImagePlus mrTrans=trans.transformImage(mri, mri);
			System.out.println("composition images");
			ImagePlus compAvant=VitimageUtils.compositeOf(rx,mri,"Avant");
			ImagePlus compApres=VitimageUtils.compositeOf(rx,mrTrans,"Apres");
			System.out.println("sauvegarde images");
			IJ.saveAsTiff(compAvant, "/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/compAvant_"+spec+".tif");
			IJ.saveAsTiff(compApres, "/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/compApresIter_"+it+("_SIG"+sigma)+"_"+spec+(fixCenter ? "_FIX" : "_NO")+(gauss ? "_GAUSS" : "")+".tif");
		}
	}
	
	
	public static void computeFieldBiasBasedOnMultipleCorrespondances(int iteration,boolean fixCenter,boolean gauss,double sigma) {
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		int nSpec=6;
		
		ImagePlus imgRef1=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/rx_"+specimen[0]+".tif");
		ImagePlus imgRef=VitimageUtils.Sub222(imgRef1);
		double[]voxRough=VitimageUtils.getVoxelSizes(imgRef);
		double voxZ=voxRough[2];
		double voxX=voxRough[0];
		double voxY=voxRough[1];
		Point3d[][][]pts=new Point3d[(iteration+1)][nSpec][];
		int total=0;
		for(int i=0;i<nSpec;i++) {
			String spec=specimen[i];
			for(int iter=0;iter<(iteration+1);iter++) {
				if(new File("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/points_"+spec+"_iteration_"+iteration+".txt").exists()) {
					pts[iter][i]=VitimageUtils.readPoint3dArrayInFile("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/points_"+spec+"_iteration_"+iteration+".txt");
					total+=pts[iter][i].length/2;
				}
			}
		}//		
		Point3d [][]correspondancePoints=new Point3d[(iteration+1)][total];
		int incr=0;
		
		for(int i=0;i<nSpec;i++) {
			for(int iter=0;iter<(iteration+1);iter++) {
				for(int j=0;j<pts[iter][i].length/2;j++) {
					correspondancePoints[1][incr]=pts[iter][i][2*j];//IRM
					correspondancePoints[0][incr++]=pts[iter][i][2*j+1];//RX
				}
			}
		}
		
		System.out.println("Total avant fix="+total);
		if(fixCenter) {
			Point3d [][]correspondancePointsSave=new Point3d[2][total];
			for(int i=0;i<correspondancePoints.length;i++)for(int j=0;j<correspondancePoints[0].length;j++)
				correspondancePointsSave[i][j]=new Point3d(correspondancePoints[i][j].x,correspondancePoints[i][j].y,correspondancePoints[i][j].z);
			double[]slices= {185*voxZ,200*voxZ,230*voxZ};
			double[]xiz= {60*voxX,100*voxX,120*voxX,140*voxX,180*voxX};
			double[]yiz= {60*voxX,100*voxX,120*voxX,140*voxX,180*voxX};
			int nPer=(int)Math.round(total*1.0/(4.0*45));
			int incr2=0;
			correspondancePoints=new Point3d[2][total+3*5*5*nPer];
			System.out.println("A ajouter = "+(3*5*5*nPer));
			System.out.println("Final = "+(total+3*5*5*nPer));
			
			for(int np=0;np<nPer;np++)for(double x : xiz)for(double y : yiz)for(double z:slices) {
				correspondancePoints[1][incr2]=correspondancePoints[0][incr2]=new Point3d(x+np*voxX,y+np*voxY,z+2*np*voxZ);
				incr2++;
			}
			System.out.println("Apres copie fix, incr2="+incr2);
			for(int i=0;i<total;i++) {
				for(int j=0;j<2;j++) {
					correspondancePoints[j][i+incr2]=correspondancePointsSave[j][i];
				}
				
			}
		}
		
		ImagePlus img=(ImagePlus)(VitimageUtils.getCorrespondanceListAsImagePlus(imgRef,correspondancePoints,new double[] {1,1,1},-1)[0]) ;
		IJ.saveAsTiff(img, "/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/corrPoints_"+iteration+"_"+(fixCenter ? "_FIX" : "_NO")+".tif");
		System.out.println("En effet, incr="+incr+" et total="+total);
		ItkTransform trans=new ItkTransform(new DisplacementFieldTransform(ItkTransform.computeDenseFieldFromSparseCorrespondancePoints(correspondancePoints, imgRef, sigma, false)));
		System.out.println("Here8");
		if(gauss)trans=ItkTransform.smoothDeformationTransform(trans,sigma/2,sigma/2,sigma/2);
		trans.writeAsDenseField("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/champ_"+iteration+("_SIG"+sigma)+(fixCenter ? "_FIX" : "_NO")+(gauss ? "_GAUSS" : "")+".tif", imgRef);
		
		System.out.println("Here9");
		trans.showAsGrid3D(imgRef, 10,"Field"+(fixCenter ? "_FIX" : "_NO")+(gauss ? "_GAUSS" : ""),100);
	}
	
	
	
	
	public static void takeInputCorrespondanceForBiasEstimation() {
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		int nSpec=12;
		int iteration=1;
		ImagePlus imgRef;
		Point3d[]pts;
		for(int i=7;i<nSpec;i++) {
			
			System.out.println("Traitement "+specimen[i]);
			String spec=specimen[i];
			String repSpec="/home/fernandr/Bureau/Traitements/Cep5D/"+spec;
			System.out.println("Load");
			ImagePlus mri=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/mri_"+spec+".tif");
			ImagePlus rx=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/rx_"+spec+".tif");
			ImagePlus composite=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/composite_"+spec+".tif");
			ImagePlus imgplus=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/compApresIter_0_SIG10.0_"+spec+"_FIX_GAUSS.tif");
			imgplus.show();
			pts=VitiDialogs.waitForPointsUIUntilClickOnSlice1(mri,rx,composite,true);//PT 1 = MRI  PT2=RX
			VitimageUtils.writePoint3dArrayInFile(pts,"/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/points_"+spec+"_iter_"+iteration+".txt");	
			imgplus.close();
		}//		ItkTransform.computeDenseFieldFromSparseCorrespondancePoints(correspondancePoints, imgRef, sigma, zeroPaddingOutside)
	}
	
	
	public static void testStrategyField() {
		ItkTransform field=ItkTransform.readAsDenseField("/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/champ_0_SIG10.tif");
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		for(int i=0;i<12;i++) {
			System.out.println("Traitement "+specimen[i]);
			String spec=specimen[i];
			String repSpec="/home/fernandr/Bureau/Traitements/Cep5D/"+spec;
			ImagePlus imgRX=IJ.openImage(repSpec+"/Source_data/PHOTOGRAPH/Computed_data/1_Registration/Stacks2/imgRef_RXForMRItif.tif");
			ImagePlus imgMRI=IJ.openImage(repSpec+"/Source_data/MRI_CLINICAL/Computed_data/3_HyperImage/stack_0.tif");

			ItkTransform transMRItoRX= new ItkTransform(
				ItkTransform.readTransformFromFile(repSpec+"/Source_data/PHOTOGRAPH/Computed_data/1_Registration/Transforms2/trans_MRI_AUTO.txt"));
			ItkTransform transMRIfromMachine=new ItkTransform((ItkTransform.readTransformFromFile(
				repSpec+"/Source_data/MRI_CLINICAL/Computed_data/1_Transformations/transformation_0.txt")).getInverse());

			transMRIfromMachine.addTransform(ItkTransform.itkTransformFromCoefs(new double[] {1,0,0,-175,0,1,0,-175,0,0,1,-153.6}));
			transMRIfromMachine.addTransform(field);
			transMRIfromMachine.addTransform(ItkTransform.itkTransformFromCoefs(new double[] {1,0,0,-175,0,1,0,-175,0,0,1,-153.6} ).getInverse( ));
			transMRIfromMachine.addTransform(new ItkTransform(ItkTransform.readTransformFromFile(
				repSpec+"/Source_data/MRI_CLINICAL/Computed_data/1_Transformations/transformation_0.txt")));
			transMRIfromMachine.addTransform(transMRItoRX);
			ImagePlus resAvecDef=transMRIfromMachine.transformImage(imgRX, imgMRI);
			ImagePlus resSansDef=transMRItoRX.transformImage(imgRX, imgMRI);
		
			ImagePlus compAvant=VitimageUtils.compositeOf(imgRX,resSansDef,"Avant");
			ImagePlus compApres=VitimageUtils.compositeOf(imgRX,resAvecDef,"Apres");
			System.out.println("sauvegarde images");
			IJ.saveAsTiff(compAvant, "/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/Better/Avant_"+spec+".tif");
			IJ.saveAsTiff(compApres, "/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/Better/Apres_"+spec+".tif");
		}
		return;	
	}
	
	
	
	/*Pour construire les images dans le cadre bias, j ai :
	* Pris la transformation machine inverse
	Je lui ai ajoute un decalage 

	* Cette transformation me permet de reechantillonner IRM dans la geometrie init
	Donc techniquement, je pourrais :
	ImagePlus imgMRI=IJ.openImage(repSpec+"/Source_data/MRI_CLINICAL/Computed_data/3_HyperImage/stack_0.tif");
	T=transMRIMachine.add(decalage).add(field).add(decalageinverse).add(transMRImachineinverse).add(transToRX)
	T.transformImage(mri)
	composite(rx,mri)
	*/
	
	
	
	
	public static void computeImageForClinicalMRIBiasEstimation() {
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		for(int i=0;i<12;i++) {
			System.out.println("Traitement "+specimen[i]);
			String spec=specimen[i];
			String repSpec="/home/fernandr/Bureau/Traitements/Cep5D/"+spec;
			System.out.println("Load");
			ItkTransform transMRIfromMachine=new ItkTransform((ItkTransform.readTransformFromFile(repSpec+"/Source_data/MRI_CLINICAL/Computed_data/1_Transformations/transformation_0.txt")).getInverse());
			transMRIfromMachine.addTransform(ItkTransform.itkTransformFromCoefs(new double[] {1,0,0,-175,0,1,0,-175,0,0,1,-153.6}));
			ItkTransform transRXtoMRI= new ItkTransform((ItkTransform.readTransformFromFile(repSpec+"/Source_data/PHOTOGRAPH/Computed_data/1_Registration/Transforms2/trans_MRI_AUTO.txt")).getInverse());
			transRXtoMRI.addTransform(transMRIfromMachine);
			ImagePlus imgRX=IJ.openImage(repSpec+"/Source_data/PHOTOGRAPH/Computed_data/1_Registration/Stacks2/imgRef_RXForMRItif.tif");
			ImagePlus imgMRI=IJ.openImage(repSpec+"/Source_data/MRI_CLINICAL/Computed_data/3_HyperImage/stack_0.tif");

			ImagePlus imgRef=IJ.createImage("", 512, 512, 768, 8);
			VitimageUtils.adjustImageCalibration(imgRef, imgMRI);
			System.out.println(TransformUtils.stringVector(VitimageUtils.getVoxelSizes(imgMRI),""));
			System.out.println("Trans 1");
			imgRX=transRXtoMRI.transformImage(imgRef, imgRX);
			imgMRI=transMRIfromMachine.transformImage(imgRef,imgMRI);
			ImagePlus comp=VitimageUtils.compositeOf(imgRX, imgMRI);
			comp.setTitle(spec);
			IJ.saveAsTiff(imgMRI, "/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/mri_"+spec+".tif");
			IJ.saveAsTiff(imgRX, "/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/rx_"+spec+".tif");
			IJ.saveAsTiff(comp, "/home/fernandr/Bureau/Traitements/Cep5D/FieldsBias/composite_"+spec+".tif");
		}
	}
	
	public static void viewMRI() {
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		String dir="/home/fernandr/Bureau/Test/Bias/";
		String tmp;
		for(int i=0;i<12;i++) {
			System.out.println("Traitement "+specimen[i]);
			String spec=specimen[i];
			ImagePlus imgRef=IJ.openImage(
					"/home/fernandr/Bureau/Traitements/Cep5D/"+spec+"/Source_data/PHOTOGRAPH/Computed_data/1_Registration/Stacks2/imgRef_RXForMRItif.tif");
			ImagePlus imgMov=IJ.openImage(
					"/home/fernandr/Bureau/Traitements/Cep5D/"+spec+"/Source_data/PHOTOGRAPH/Computed_data/1_Registration/Stacks2/imgMov_MRI_registeredAuto.tif");
	
			imgRef.setDisplayRange(0,255);
			imgMov.setDisplayRange(0,i==3 ? 70 : 180);
			IJ.run(imgMov,"8-bit","");
			ImagePlus res=VitimageUtils.compositeNoAdjustOf(imgRef,imgMov);
		    VitimageUtils.showImageUntilItIsClosed(res,spec);
		}			
	}
	
	
	public static void pequenaJala() {
		String []specimen= {"CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		String dir="/home/fernandr/Bureau/Test/Bias/";
		String tmp;
		for(int i=0;i<12;i++) {
			System.out.println("Traitement "+specimen[i]);
			String spec=specimen[i];
			ImagePlus imgRefHigh=IJ.openImage(
					"/home/fernandr/Bureau/Traitements/Cep5D/"+spec+"/Source_data/PHOTOGRAPH/Computed_data/1_Registration/Stacks2/imgRef_RXForMRItif.tif");
			ImagePlus imgMovHigh=IJ.openImage(
					"/home/fernandr/Bureau/Traitements/Cep5D/"+spec+"/Source_data/PHOTOGRAPH/Computed_data/1_Registration/Stacks2/imgMov_MRI_registeredAuto.tif");
			imgMovHigh.setTitle("RES_0");
			imgRefHigh.setTitle("REF");
			ImagePlus imgRef=VitimageUtils.Sub222(imgRefHigh);
			ImagePlus imgMov=VitimageUtils.Sub222(imgMovHigh);
			tmp=spec+"REF.tif";imgRefHigh.setTitle(tmp);IJ.saveAsTiff(imgRefHigh,dir+tmp);
			tmp=spec+"RES_0.tif";imgMovHigh.setTitle(tmp);IJ.saveAsTiff(imgMovHigh,dir+tmp);
			int sigma=45;
			int nit=7;
			for(int config=0;config<3;config++) {
				
				int levelMax=3-config/2;
				int levelMin=3-(config+1)/2;
				System.out.println("Sigma="+sigma+" nit="+nit+" levelMin="+levelMin+" levelMax="+levelMax);
				int viewSlice=120;
				int nbIterations=nit;//14
				int neighXY=2;	int neighZ=0;
				int bSXY=7;		int bSZ=7;
				int strideXY=2;		int strideZ=3;
				ItkTransform transRet=null;
				BlockMatchingRegistration bmRegistration=new BlockMatchingRegistration(imgRef,imgMov,Transformation3DType.DENSE,MetricType.SQUARED_CORRELATION,
							0,sigma,levelMin,levelMax,nbIterations,viewSlice,null,neighXY,neighZ,bSXY,bSZ,strideXY,strideZ);
		//		bmRegistration.flagSingleView=true;
				bmRegistration.displayRegistration=false;
				bmRegistration.displayR2=false;
				transRet=bmRegistration.runMultiThreaded(null);
				//bmRegistration.closeLastImages();
				bmRegistration.freeMemory();
				ImagePlus res=transRet.transformImage(imgRefHigh, imgMovHigh);
				res.setDisplayRange(0, 255);
				IJ.run(res,"8-bit","");
				tmp=spec+"RES_MAX_"+levelMax+"_MIN_"+levelMin+".tif";res.setTitle(tmp);IJ.saveAsTiff(res,dir+tmp);
			}
		}
	}
	
	


	public static void chiasse7() {
		String []specimen= {"CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		for(int i=0;i<12;i++) {
			System.out.println("Traitement "+specimen[i]);
			ImagePlus imgMask=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Source_data/RX/Computed_data/0_Stacks/mask.tif");
			VitimageUtils.adjustImageCalibration(imgMask, new double[] {0.6,0.6,0.6},"mm");
			IJ.saveAsTiff(imgMask,"/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Source_data/RX/Computed_data/0_Stacks/mask.tif");
			imgMask.close();
			System.out.print("1");

			ImagePlus imgRX=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Source_data/RX/Computed_data/0_Stacks/RX.tif");
			VitimageUtils.adjustImageCalibration(imgRX, new double[] {0.6,0.6,0.6},"mm");
			IJ.saveAsTiff(imgRX,"/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Source_data/RX/Computed_data/0_Stacks/RX.tif");
			imgRX.close();
			System.out.print("2");

			ImagePlus imgHyp=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Source_data/RX/Computed_data/3_HyperImage/hyperImage.tif");
			VitimageUtils.adjustImageCalibration(imgHyp, new double[] {0.6,0.6,0.6},"mm");
			IJ.saveAsTiff(imgHyp,"/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Source_data/RX/Computed_data/3_HyperImage/hyperImage.tif");
			imgHyp.close();
			System.out.print("3");
		
		
		}
	}
	
	
	public static void chiasse4() {
		ImagePlus test=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/For_Cedric/2_After_Segmentation/CEP022_APO3_after_step_2_segmentation.tif");		
		IJ.run(test,"8-bit","");
		ImagePlus[]result=VitimageUtils.meanAndVarOnlyValidValuesByte(test,5);
		result[0].show();
		result[1].show();
	}
	
	
	public static void chiasse3() {
		ImagePlus test=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/For_Cedric/2_After_Segmentation/CEP022_APO3_after_step_2_segmentation.tif");
		test.show();
		int nExp=4;
		int[]tabDiamXY=new int[] {5,6,7,8}; 
		int[]tabZup=new int[] {1,1,1,1,2,2,2,2,2,2,2,2};
		int[]tabZdown=new int[] {2,2,2,2,1,3,1,3,1,3,1,3};
		boolean[]tabBool=new boolean[] {true,true,true,true,false,false,false,false,false,false,false,false};
		ImagePlus res1;
		for(int i=0;i<nExp;i++) {
			System.out.println("");
			System.out.println("Traitements jeu de parametres "+i+" : "+"-"+tabDiamXY[i]+"--"+tabZup[i]+"-"+tabZdown[i]+"--"+tabBool[i]);
			System.out.println();
			res1=VitimageUtils.normalizationSliceRGB(test,tabDiamXY[i],tabDiamXY[i],tabZup[i],tabZdown[i],tabBool[i]);
			res1.setTitle(""+tabDiamXY[i]+"-"+tabDiamXY[i]+"--"+tabZup[i]+"-"+tabZdown[i]+"--"+tabBool[i]+"_RECTVERS.tif");
			IJ.saveAsTiff(res1,"/home/fernandr/Bureau/Test/TestEqualization/"+res1.getTitle());
		}

	}

	public static void chiasse2() {
		String []specimen= {"CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		for(int i=0;i<1;i++) {//specimen.length	
			System.out.println("\n\n\nTraitement specimen "+specimen[i]+" ");
			String pathToData="/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i];
			String slash="/";
			ImagePlus segWood=IJ.openImage(pathToData+slash+"Computed_data"+slash+"2_Segmentation"+slash+"seg_low_wood_radius_4.tif");
		 // create structuring element (cube of radius 'radius')
			Strel3D str=inra.ijpb.morphology.strel.CuboidStrel.fromRadiusList(2,2, 1);
			ImagePlus img =new ImagePlus("",Morphology.closing(segWood.getImageStack(),str));
			str=inra.ijpb.morphology.strel.SquareStrel.fromRadius(2);
			img =new ImagePlus("",Morphology.erosion(segWood.getImageStack(),str));
			segWood.show();
			img.show();
			VitimageUtils.waitFor(100000);
		}
		
		
	}
	

	
	public static void makeTrainingHybride() {
		String []specimen= {"CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP011_AS1","CEP012_AS2","CEP013_AS3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		//(84-2) par 10  +1 = 8 : 2 12 22 32 42 52 62 72 82 
		//81-2 par 10 +1 = 8 = 2 12 22 32 42 52 62 72
		ImagePlus imgTab[]=new ImagePlus[12];
		int[]tabDim=new int[12];
		int nbTotOut=0;
		int max=0;
		int incr=0;
		int curSli=0;
		
		//Preparer les stacks
		for(int i=0;i<12;i++) {
			imgTab[i]=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Computed_data/0_Stacks/Stack_low_res.tif");
			tabDim[i]=imgTab[i].getStackSize();
			nbTotOut+=(tabDim[i]-2)/10+1;
			if(nbTotOut>max)max=nbTotOut;
		}
		
		//Recuperer les slices concernees
		ImagePlus []tabOut=new ImagePlus[nbTotOut];
		for(int it=0;it<max;it++) {
			curSli=2+10*it;
			for(int sp=0;sp<12;sp++) {
				if(curSli<=tabDim[sp]) {
					System.out.println("Out slice "+(incr+1)+" = img"+sp+"["+curSli+"]");
					imgTab[sp].setSlice(curSli);
					tabOut[incr]=imgTab[sp].crop();
					incr++;
				}
			}
		}
		ImagePlus ret=Concatenator.run(tabOut);
		ret.show();
	}

	
	public static void inspectAllPhoto() {
		int stepEnd=3;//
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3" ,"CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		for(int i=0;i<specimen.length;i++) {//	
			System.out.println("\n\n\nTraitement specimen "+specimen[i]+" jusqu a step "+stepEnd);
			String pathToData="/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i];
			ImagePlus p2=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Computed_data/1_Registration/Stacks/img_low_after_step_1.tif");
			ImagePlus p=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Computed_data/2_Segmentation/wood_only_rgb_radius_4_pruned.tif");
			//IJ.run(p,"8-bit","");
			//IJ.run(p2,"8-bit","");
			//p=VitimageUtils.compositeOf(p, p2);
			p.show();
			p.setTitle("ppp.tif"+specimen[i]);
			boolean flag=false;
			int incr=0;
			while(!flag) {
				if(WindowManager.getImage("ppp.tif"+specimen[i])==null)flag=true;
				VitimageUtils.waitFor(1000);
				System.out.println(incr++);
			}
			p.close();
		}		
	}
	
	public static void publishResults() {
		int publishedStep=3;
		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3" ,"CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		for(int i=0;i<specimen.length;i++) {
			System.out.println(i);
			if(publishedStep==1) {			
				String outputDir="/home/fernandr/Bureau/Traitements/Cep5D/For_Cedric/1_After_PVC_Alignement/";
				String inputData="/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Computed_data/1_Registration/Stacks/img_low_after_step_1.tif";
				ImagePlus img=IJ.openImage(inputData);
				img.setTitle(specimen[i]+"_after_step_1_pvc_alignement.tif");
				IJ.saveAsTiff(img, outputDir+specimen[i]+"_after_step_1_pvc_alignement.tif");
			}
			if(publishedStep==2) {			
				String outputDir="/home/fernandr/Bureau/Traitements/Cep5D/For_Cedric/2_After_Segmentation/";
				File f=new File(outputDir);
				f.mkdir();
				String inputData="/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Computed_data/2_Segmentation/wood_only_rgb_radius_4_pruned.tif";
				ImagePlus img=IJ.openImage(inputData);
				img.setTitle(specimen[i]+"_after_step_2_segmentation.tif");
				IJ.saveAsTiff(img, outputDir+specimen[i]+"_after_step_2_segmentation.tif");
			}
			if(publishedStep==3) {			
				String outputDir="/home/fernandr/Bureau/Traitements/Cep5D/For_Cedric/3_After_Equalization/";
				File f=new File(outputDir);
				f.mkdir();
				String inputData="/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i]+"/Computed_data/2_Segmentation/wood_only_rgb_equalized.tif";
				ImagePlus img=IJ.openImage(inputData);
				img.setTitle(specimen[i]+"_after_step_3_equalization.tif");
				IJ.saveAsTiff(img, outputDir+specimen[i]+"_after_step_3_equalization.tif");
			}
		}
	}
	
	public static void makeAllPhoto() {
		int stepEnd=7;//

		String []specimen= {"CEP011_AS1","CEP012_AS2","CEP013_AS3" ,"CEP014_RES1","CEP015_RES2","CEP016_RES3","CEP017_S1","CEP018_S2","CEP019_S3","CEP020_APO1","CEP021_APO2","CEP022_APO3"};
		for(int i=0;i<12;i++) {//specimen.length	
			System.out.println("\n\n\nTraitement specimen "+specimen[i]+" jusqu a step "+stepEnd);
			String pathToData="/home/fernandr/Bureau/Traitements/Cep5D/"+specimen[i];
			Photo_Slicing_Seq photo=new Photo_Slicing_Seq(pathToData,Capillary.HAS_NO_CAPILLARY,SupervisionLevel.AUTONOMOUS,specimen[i],ComputingType.COMPUTE_ALL);
			photo.start(stepEnd);
		}
	}
	
	
	public static void testCon() {
		ImagePlus imgTest=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/CEP019_S3_Martyr/Computed_data/1_Registration/maskOutPVC.tif");		
		ImagePlus res=VitimageUtils.connexe(imgTest, 200, 256, 0, 10E10, 6, 1, false);
		res.resetDisplayRange();
		res.show();
	}
	

	
	
	public static void testAssemblage() {
		ImagePlus canalR=IJ.openImage("/home/fernandr/Bureau/canalR.tif");
		ImagePlus canalG=IJ.openImage("/home/fernandr/Bureau/canalR.tif");
		ImagePlus canalJet=IJ.openImage("/home/fernandr/Bureau/canalJet.tif");
		IJ.run(canalJet,"8-bit","");
		ImagePlus maskJet=VitimageUtils.thresholdByteImage(canalJet, 1, 256);
		IJ.run(maskJet,"Invert","");
		IJ.run(maskJet, "Divide...", "value=255");
		canalR = new ImageCalculator().run("Multiply create", canalR, maskJet);
		canalG = new ImageCalculator().run("Multiply create", canalG, maskJet);
		IJ.run(canalR,"Red","");
		IJ.run(canalG,"Green","");
		IJ.run(canalJet,"Fire","");
		ImagePlus result=RGBStackMerge.mergeChannels(new ImagePlus[] {canalR,canalG,canalJet},false);
		result.show();
	}
	
	public static void testAssemblage2() {
		ImagePlus canalR=IJ.openImage("/home/fernandr/Bureau/canalR.tif");
		ImagePlus canalG=IJ.openImage("/home/fernandr/Bureau/canalR.tif");
		ImagePlus canalCorrR=IJ.openImage("/home/fernandr/Bureau/corrRed.tif");
		ImagePlus canalCorrG=IJ.openImage("/home/fernandr/Bureau/corrGreen.tif");
		ImagePlus canalCorrB=IJ.openImage("/home/fernandr/Bureau/corrBlue.tif");
	
		
		ImagePlus finalR = new ImageCalculator().run("Add create", canalR, canalCorrR);
		ImagePlus finalG = new ImageCalculator().run("Add create", canalG, canalCorrG);
		ImagePlus finalB = canalCorrB;
		IJ.run(finalR,"Red","");
		IJ.run(finalG,"Green","");
		IJ.run(finalB,"Blue","");
		ImagePlus result=RGBStackMerge.mergeChannels(new ImagePlus[] {finalR,finalG,finalB},false);
		result.show();
	}
	

	public static void testWeka() {
		
//		ImagePlus imgTest=IJ.openImage("/home/fernandr/Bureau/testST.tif");		
		ImagePlus imgTest=IJ.openImage("/home/fernandr/Bureau/Traitements/Cep5D/CEP019_S3_Martyr/Computed_data/0_Stacks/Stack_low_res.tif");		
		ImagePlus result=VitimageUtils.makeWekaSegmentation(imgTest,"/home/fernandr/Bureau/Traitements/Cep5D/CEP019_S3_Martyr/classifier_bois.model");
		imgTest.show();
		IJ.run(result, "8-bit", "");
		result.show();
	}

	
	public static void makeMartyr() {
		String repSource="/mnt/DD_COMMON/Data_VITIMAGE/Ceps_par_specimen/S3/";
		String repDest="/mnt/DD_COMMON/Data_VITIMAGE/Ceps_par_specimen/S3_Martyr/";
		for(int i=0;i<200;i++) {
			System.out.println(i);
			String numb=(i<100 ? "00"+i : "0"+i);
			String totSource=repSource+"DSC_"+numb+".jpg";
			String totDest=repDest+"DSC_"+numb+".tif";
			File f=new File(totSource);
			if(f.exists()) {
				ImagePlus imp = IJ.openImage(totSource);
				System.out.println("Ouverture image "+totSource);
				IJ.run(imp, "Scale...", "x=0.25 y=0.25 width=1500 height=1000 interpolation=Bilinear average create");
				ImagePlus img=IJ.getImage();
				IJ.saveAs(img, "Tiff", totDest);
				img.changes=false;
				img.close();
				
			}
		}		
	}
	

	public static void produceStackTestForPhoto() {
		String rep="/home/fernandr/Bureau/Test/TestPhotoStack/";
		ImagePlus imgRef=IJ.openImage(rep+"modele_pour_generer_stack_sub.tif");
		int nbImgs=50;
		double varX=100;
		double varY=100;
		double varTeta=20;
		IJ.run("Colors...", "foreground=white background=white selection=yellow");
		
		for(int i =0;i<nbImgs;i++) {			
			ImagePlus imgTemp=imgRef.duplicate();
			double dx=(-0.5*Math.random()) * varX;
			double dy=(-0.5*Math.random()) * varY;
			double dteta=(-0.5*Math.random()) * varTeta;
			System.out.println("i="+i+" .... teta="+dteta+" x="+dx+" y="+dy);
			IJ.run(imgTemp, "Rotate... ", "angle="+dteta+" grid=1 interpolation=Bilinear fill");
//			IJ.run(imgTemp, "Translate...", "x="+dx+" y="+dy+" interpolation=Bilinear fill");
			IJ.saveAsTiff(imgTemp,rep+"Stack/IMG_"+i+".tif");
		}
	}
	
	

	
	
	public static void makeAllTiImport() {
		String []specimen= {"B079_NP","B080_NP", "B081_NP", "B089_EL" ,"B090_EL" ,"B091_EL" ,"B098_PCH", "B099_PCH" ,"B100_PCH"};
//		int[]days= {0,35,70,105,133};
		int[]days= {105};

		for(int i=0;i<specimen.length;i++) {	
			for(int j=0;j<days.length;j++) {	
				for(int k=0;k<50;k++)System.out.println();
				System.out.println("Traitement specimen "+specimen[i]+" at day "+days[j]);
				String pathToData="/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i]+"/Source_data/J"+days[j];
				File f=new File(pathToData);
				if(f.exists()) {
					System.out.println("\n\n\nProcessing "+pathToData);			
					MRI_T1_Seq mri1=new MRI_T1_Seq(pathToData+"/Source_data/MRI_T1_SEQ",
							Capillary.HAS_CAPILLARY,SupervisionLevel.GET_INFORMED,specimen[i]+"_J"+days[j]+"_MRI_T1",ComputingType.COMPUTE_ALL);
					mri1.start();
					MRI_T2_Seq mri2=new MRI_T2_Seq(pathToData+"/Source_data/MRI_T2_SEQ",
							Capillary.HAS_CAPILLARY,SupervisionLevel.GET_INFORMED,specimen[i]+"_J"+days[j]+"_MRI_T2",ComputingType.COMPUTE_ALL);
					mri2.start();
				}
			}
		}
	}
	
	public static void makeAllGeImport() {
		String []specimen= {"B079_NP","B080_NP", "B081_NP", "B089_EL" ,"B090_EL" ,"B091_EL" ,"B098_PCH", "B099_PCH" ,"B100_PCH"};
		int[]days= {0,35,70,105,133};
//		int[]days= {105};

		for(int i=0;i<specimen.length;i++) {	
			for(int j=0;j<days.length;j++) {	
				for(int k=0;k<50;k++)System.out.println();
				System.out.println("Traitement specimen "+specimen[i]+" at day "+days[j]);
				String pathToData="/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i]+"/Source_data/J"+days[j];
				File f=new File(pathToData);
				if(f.exists()) {
					System.out.println("\n\n\nProcessing "+pathToData);			
					MRI_Ge3D mri1=new MRI_Ge3D(pathToData+"/Source_data/MRI_GE3D",
							Capillary.HAS_CAPILLARY,SupervisionLevel.GET_INFORMED,specimen[i]+"_J"+days[j]+"_MRI_GE2D",ComputingType.COMPUTE_ALL);
					mri1.start();
				}
			}
		}
	}
	
	
	
	public static void makeAllAssistedDetections(){
//		String []specimen= {"B079_NP","B080_NP", "B081_NP", "B089_EL" ,"B090_EL" ,"B091_EL" ,"B098_PCH", "B099_PCH" ,"B100_PCH"};
//		int[]days= {0,35,70,105,133};
		String []specimen= { "B099_PCH" };
		int[]days= {35};
		for(int i=0;i<specimen.length;i++) {	
			for(int j=0;j<days.length;j++) {	
				for(int k=0;k<50;k++)System.out.println();
				System.out.println("Traitement specimen "+specimen[i]+" at day "+days[j]);
				String pathToData="/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i]+"/Source_data/J"+days[j];
				File f=new File(pathToData);
				if(f.exists()) {
					System.out.println("\n\n\nProcessing "+pathToData);			
					Vitimage4D viti = new Vitimage4D(VineType.CUTTING,0,"/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i]+"/Source_data/J"+days[j],
							specimen[i]+"_"+days[j],ComputingType.COMPUTE_ALL);
					viti.start(5);
				}
			}
		}
	}
	
	
	
	public static void finishAll4D(){
		String []specimen= {"B079_NP","B080_NP", "B081_NP", "B089_EL" ,"B090_EL" ,"B091_EL" ,"B098_PCH", "B099_PCH" ,"B100_PCH"};
		int[]days= {0,35,70,105,133};
		for(int i=0;i<specimen.length;i++) {	
			for(int j=0;j<days.length;j++) {	
				if(! ((i<0) || ((i==0) && (j<0)))) {
					for(int k=0;k<50;k++)System.out.println();
					System.out.println("Traitement specimen "+specimen[i]+" at day "+days[j]);
					String pathToData="/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i]+"/Source_data/J"+days[j];
					File f=new File(pathToData);
					if(f.exists()) {
						System.out.println("\n\n\nProcessing "+pathToData);			
						Vitimage4D viti = new Vitimage4D(VineType.CUTTING,0,"/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i]+"/Source_data/J"+days[j],
								specimen[i]+"_"+days[j],ComputingType.COMPUTE_ALL);
						viti.start(10);
					}
				}
			}
		}
	}
	
	
	
	public static void makeAll5D(){
		String []specimen= {"B099_PCH" ,"B091_EL" ,"B079_NP","B080_NP", "B081_NP", "B089_EL" ,"B090_EL" ,"B098_PCH", "B100_PCH"};
//		String []specimen= {"B099_PCH"};
		for(int i=0;i<specimen.length;i++) {	
			if(! (i<0) ) {
				for(int k=0;k<50;k++)System.out.println();
				System.out.println("Traitement specimen "+specimen[i]);
				String pathToData="/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i];
				System.out.println("\n\n\nProcessing "+pathToData);			
				Vitimage5D viti = new Vitimage5D(VineType.CUTTING,"/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i],
						specimen[i],ComputingType.COMPUTE_ALL);
				viti.start(5);
			}
		}
	}
	
	




	public static void makeAllAxisAndInocDetection() {
		String []specimen= {"B079_NP","B080_NP", "B081_NP", "B089_EL" ,"B090_EL" ,"B091_EL" ,"B098_PCH", "B099_PCH" ,"B100_PCH"};
		for(int i=0;i<specimen.length;i++) {	
			String pathToData="/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i];
			System.out.println("\n\n\nProcessing "+pathToData);			

			Vitimage4D viti = new Vitimage4D(VineType.CUTTING,0,"/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/"+specimen[i],
					specimen[i],ComputingType.COMPUTE_ALL);			
			viti.start(5);//Stands for breaking before the beginning of fine automatic registration part
		}
	}
	
	
	
	public static void scheisse() {
		MRI_T1_Seq mri=new MRI_T1_Seq("/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/B099_PCH/Source_data/J0/Source_data/MRI_T1_SEQ",
				Capillary.HAS_CAPILLARY,SupervisionLevel.GET_INFORMED,"B041_DS_J218_MRI_T1",ComputingType.COMPUTE_ALL);
mri.start();
		VitimageUtils.detectAxis(mri,50);
		
		
	}
	
	public static void importOnlyGe3dForVerif() {
		String repSource="/mnt/DD_COMMON/Data_VITIMAGE/Export_IRM_TEMP";
		String repCible="/mnt/DD_COMMON/Data_VITIMAGE/Export_IRM_TEMP/Pour_Cedric";
		String[]specimen=new String[] {"B079_NP" ,"B080_NP", "B081_NP", "B089_EL", "B090_EL" ,"B091_EL", "B098_PCH" ,"B099_PCH" ,"B100_PCH"};
		String[]days=new String[] {"J0", "J35", "J70" ,"J105" ,"J133"};
		ImagePlus temp;
		for(String spec : specimen) {
			for(String day : days) {
				File f=new File(repSource,spec+"/"+day+"/Ge3d/TR000100/TE000005");
				System.out.println("Traitement de "+spec+" - "+day);
				if(f.exists()) {
					temp = FolderOpener.open(f.getAbsolutePath(), "");
					temp.resetDisplayRange();
					IJ.run(temp,"Fire","");
					IJ.saveAsTiff(temp, repCible+"/"+spec+"_"+day+"_GE3D.tif");
				}				
			}			
		}
	}
	
	
	public static void testBM() {
//		ImagePlus imgRef=IJ.openImage("/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/B099_PCH/Source_data/J0/Computed_data/0_Registration/imgRegistration_acq_2_step_afterIPalignment.tif");		
//		ImagePlus imgMov=IJ.openImage("/home/fernandr/Bureau/Traitements/Bouture6D/Source_data/B099_PCH/Source_data/J0/Computed_data/0_Registration/imgRegistration_acq_2_step_afterIPalignment.tif");
		String dir="/home/fernandr/Bureau/Traitements/Cep5D/CEP020_APO1/Source_data/MRI_CLINICAL/Computed_data/3_HyperImage/";
		ImagePlus imgRef=IJ.openImage(dir+"stack_0.tif");		
		ImagePlus imgMov=IJ.openImage(dir+"stack_1.tif");	
		IJ.run(imgRef, "Scale...", "x=0.5 y=0.5 z=0.5 width="+imgRef.getWidth()+" height="+imgRef.getHeight()+" depth="+imgRef.getStackSize()+" interpolation=Bilinear average process create");
		imgRef=IJ.getImage();
		IJ.run(imgMov, "Scale...", "x=0.5 y=0.5 z=0.5 width="+imgRef.getWidth()+" height="+imgRef.getHeight()+" depth="+imgRef.getStackSize()+" interpolation=Bilinear average process create");
		imgMov=IJ.getImage();
		ItkTransform tr=BlockMatchingRegistration.blockMatchingRegistrationCepMRIData(imgRef,imgMov,null,true,true,true,true,10);
		tr=tr.flattenDenseField(imgRef);
		tr.writeAsDenseField("/home/fernandr/Bureau/Test/Field.tif", imgRef);
	}
	
	/*
	

	//Test pour vérifier le bien fondé de la matrice de passage à gauche et à droite, juste pour le reechantillonnage
	public static void testRandom4() {
		///Prendre l image blanc2
//		ImagePlus blablanc3=IJ.openImage("/home/fernandr/Bureau/Test/Random/blablanc3.tif");
		ImagePlus blamiblanc3=IJ.openImage("/home/fernandr/Bureau/Test/Random/blamiblablanc3.tif");
		ItkTransform trTest=get30Rotate();
		ImagePlus result=trTest.transformImageReech(blamiblanc3, blamiblanc3);
		blamiblanc3.show();
		result.show();
	}

		
		//Test pour vérifier le bien fondé de la matrice de passage à gauche et à droite, juste pour le reechantillonnage
	public static void testRandom3() {
		///Prendre l image blanc2
		ImagePlus blablanc3=IJ.openImage("/home/fernandr/Bureau/Test/Random/blablanc3.tif");
		ImagePlus blamiblanc3=IJ.openImage("/home/fernandr/Bureau/Test/Random/blamiblablanc3.tif");

		//La rotater puis la subsampler suivant X
		ItkTransform tr=get30Rotate();
		ImagePlus imgTrans=tr.transformImageReech(blamiblanc3, blablanc3);
		double []voxSRef=VitimageUtils.getVoxelSizes(blablanc3);
		double alpha=0;
		double []voxSMov=VitimageUtils.getVoxelSizes(blamiblanc3);
		Point3d[]tabPtRef=new Point3d [] {
			new Point3d(  (132+alpha)  * voxSRef[0],   (291+alpha)    * voxSRef[1],   0     * voxSRef[2]),
			new Point3d(  (132+alpha)  * voxSRef[0],   (116 +alpha)   * voxSRef[1],   0     * voxSRef[2]),
			new Point3d(  (303+alpha)  * voxSRef[0],   (115 +alpha)   * voxSRef[1],   0     * voxSRef[2]),
			new Point3d(  (146+alpha)  * voxSRef[0],   (369 +alpha)   * voxSRef[1],   0     * voxSRef[2]),
			new Point3d(  (317+alpha)  * voxSRef[0],   (368 +alpha)   * voxSRef[1],   0     * voxSRef[2]),
		};
		Point3d[]tabPtMov=new Point3d [] {
			new Point3d( (6 +alpha)  * voxSMov[0], (280 +alpha)    * voxSMov[1],   0     * voxSMov[2]),
			new Point3d( (23.5 +alpha)  * voxSMov[0], ( 129 +alpha)    * voxSMov[1],   0     * voxSMov[2]),
			new Point3d( (53 +alpha)  * voxSMov[0],  (214 +alpha)    * voxSMov[1],   0     * voxSMov[2]),
			new Point3d( (0.8 +alpha)  * voxSMov[0],  (355 +alpha)    * voxSMov[1],   0     * voxSMov[2]),
			new Point3d( (30.4 +alpha)  * voxSMov[0],  (440 +alpha)    * voxSMov[1],   0     * voxSMov[2]),
		};

		//Ouvrir les images, et detecter les correspondances
//		imgTrans.show();
		blablanc3.show();		
		ItkTransform trTest=ItkTransform.estimateBestRigid3D(tabPtMov,tabPtRef);
		ImagePlus result=trTest.transformImageReech(blablanc3, imgTrans);
		result.show();
//		blablanc2.show();
		
		//Construire la transformation a partir des correspondances
		//Resampler, et voir si ça marche
	}

*/

	//Test pour vérifier le bien fondé de la matrice de passage à gauche et à droite, juste pour le reechantillonnage
	public static void testRandom() {
		ImagePlus blablanc=IJ.openImage("/home/fernandr/Bureau/Test/Random/blablanc.tif");
		ImagePlus blablablanc=IJ.openImage("/home/fernandr/Bureau/Test/blablablanc.tif");
		ImagePlus imgOut1=ItkTransform.resampleImageReech(blablablanc,blablanc);
		ItkTransform tr=get30Rotate();
		ImagePlus imgOut2=ItkTransform.resampleImageReech(blablanc,imgOut1);
		blablanc.show();
		blablanc.setTitle("blablanc");
		blablablanc.setTitle("blablablanc");
		blablablanc.show();		
		imgOut1.show();
		imgOut1.setTitle("out1");
		imgOut2.show();
		imgOut2.setTitle("out2");
	}

	/*
	//Test pour vérifier le bien fondé de la matrice de passage à gauche et à droite, lorsque composé avec une transformation
	public static void testRandom2() {
		ImagePlus blablanc=IJ.openImage("/home/fernandr/Bureau/Test/Random/blablanc2.tif");
		ImagePlus blablablanc=IJ.openImage("/home/fernandr/Bureau/Test/Random/blablablanc2.tif");
		ItkTransform tr=get30Rotate();
		System.out.println(tr);
		ImagePlus imgOut1=new Duplicator().run(blablanc);
		for(int i=0;i<6;i++) {
			imgOut1=tr.transformImageReech(blablablanc,imgOut1);
			imgOut1=tr.transformImageReech(blablanc,imgOut1);
		}
		imgOut1.show();
		imgOut1.setTitle("out1");
		blablanc.show();
	}

	*/

	//Test pour vérifier le bien fondé de la matrice de passage à gauche et à droite, lorsque composé avec une transformation

	
	
	public static ItkTransform get30Rotate() {
		// (x'-50)=cosTeta(x-50)+sinTeta(x-50) => x'= xcosTeta+ysinTeta+50(1-cosTeta-sinTeta)
		// (y'-50)=-sinTeta(y-50)+cosTeta(y-50) => y'= x* -sinteta + y * cosTeta + 50 (1-cosTeta+sinTeta)
		double cosTeta=Math.sqrt(3)/2;
		double sinTeta=1.0/2;
		double[]mat=new double[] {
				cosTeta,sinTeta,0,100*(1-cosTeta-sinTeta),
				-sinTeta,cosTeta,0,100*(1-cosTeta+sinTeta),				
				0,0,1,0	
		};
		return ItkTransform.itkTransformFromCoefs(mat);
		
		
	}
	
	
	public static void chiachia() {
		ImagePlus I0=IJ.openImage("/home/fernandr/Bureau/Test/Spline/I0.tif");
		ImagePlus img0=IJ.openImage("/home/fernandr/Bureau/Test/Spline/dou0.tif");
		ImagePlus img25=IJ.openImage("/home/fernandr/Bureau/Test/Spline/dou25.tif");
		ImagePlus imgM25=IJ.openImage("/home/fernandr/Bureau/Test/Spline/douM25.tif");
		ImagePlus imgSplit=IJ.openImage("/home/fernandr/Bureau/Test/Spline/douM25UP25DOWN.tif");

		
		//Champ 1 : tout vers le bas.
		ImagePlus []imgTab1=new ImagePlus[3];
		imgTab1[0]=new Duplicator().run(img0);
		imgTab1[1]=new Duplicator().run(imgM25);
		imgTab1[2]=new Duplicator().run(img0);
		ItkTransform tr1=new ItkTransform(new Transform(ItkImagePlusInterface.convertImagePlusArrayToDisplacementField(imgTab1)));
		
		//Champ 2 : split : en bas, à droite, en haut à gauche
		ImagePlus []imgTab2=new ImagePlus[3];
		imgTab2[0]=new Duplicator().run(imgSplit);
		imgTab2[1]=new Duplicator().run(img0);
		imgTab2[2]=new Duplicator().run(img0);
		ItkTransform tr2=new ItkTransform(new Transform(ItkImagePlusInterface.convertImagePlusArrayToDisplacementField(imgTab2)));
		
		ImagePlus I1=tr1.transformImage(I0,I0);
		ImagePlus I2=tr2.transformImage(I1,I1);
		
		ItkTransform tr1add2=new ItkTransform(tr1);
		tr1add2.addTransform(new ItkTransform(tr2));

		ItkTransform tr2add1=new ItkTransform(tr2);
		tr2add1.addTransform(new ItkTransform(tr1));

		ImagePlus I2_2add1=tr2add1.transformImage(I0,I0);
		ImagePlus I2_1add2=tr1add2.transformImage(I0,I0);

		I0.setTitle("I0");
		I1.setTitle("I1");
		I2.setTitle("I2");
		I2_2add1.setTitle("I2_2add1");
		I2_1add2.setTitle("I2_1add2");
		I0.show();
		I1.show();
		I2.show();
		I2_2add1.show();
		I2_1add2.show();
	}
	
	public static void showAboutThisField(ItkTransform tr,ImagePlus imgRef, String title) {
		System.out.println("show...");
		tr.showAsGrid3D(imgRef,6,title,150);
		System.out.println("components...");
		ImagePlus[]imgTab=ItkImagePlusInterface.convertItkTransformToImagePlusArray(tr);
		for(int dim=0;dim<3;dim++) {
			imgTab[dim].show();
			IJ.run(imgTab[dim],"Fire","");
			imgTab[dim].setDisplayRange(-0.7, 0.7);
			imgTab[dim].setTitle(title+"_dim"+dim);
		}
	}
	
	public static void enormeChiasse() {
		System.out.println("Je ne suis plus sur de l'ordre dans lequel composer les transfos. C'est le sens de ce test");
		System.out.println("create...");
		ItkTransform tr02v1=new ItkTransform();
		ItkTransform tr02v2=new ItkTransform();
		ItkTransform tr20v1=new ItkTransform();
		ItkTransform tr20v2=new ItkTransform();
		ItkTransform tr00v1=new ItkTransform();
		ItkTransform tr00v2=new ItkTransform();

		System.out.println("open...");
		String ch="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_0_to_1.mhd";		
		ItkTransform tr01=ItkTransform.readFromDenseFieldWithITKImporter(ch);
		ch="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_1_to_0.mhd";		
		ItkTransform tr10=ItkTransform.readFromDenseFieldWithITKImporter(ch);

		System.out.println("open...");
		ch="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_1_to_2.mhd";		
		ItkTransform tr12=ItkTransform.readFromDenseFieldWithITKImporter(ch);
		ch="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_2_to_1.mhd";		
		ItkTransform tr21=ItkTransform.readFromDenseFieldWithITKImporter(ch);
		
		System.out.println("add...");
		tr02v1.addTransform(tr01);
		tr02v1.addTransform(tr12);

		tr02v2.addTransform(tr12);
		tr02v2.addTransform(tr01);


		System.out.println("add...");
		tr20v1.addTransform(tr21);
		tr20v1.addTransform(tr10);

		tr20v2.addTransform(tr10);
		tr20v2.addTransform(tr21);

		System.out.println("add...");
		tr00v1.addTransform(tr01);
		tr00v1.addTransform(tr10);
		tr00v2.addTransform(tr10);
		tr00v2.addTransform(tr01);

		
		System.out.println("open...");
		ImagePlus imgD0=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D0_registered.tif");
		ImagePlus imgD2=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D2_registered.tif");
		
		System.out.println("transform...");
		ImagePlus imgD0to2_v1=tr02v1.transformImage(imgD0, imgD0);
		ImagePlus imgD0to2_v2=tr02v2.transformImage(imgD0, imgD0);
		System.out.println("transform...");
		ImagePlus imgD2to0_v1=tr20v1.transformImage(imgD2, imgD2);
		ImagePlus imgD2to0_v2=tr20v2.transformImage(imgD2, imgD2);
		System.out.println("transform...");
		ImagePlus imgD0to0_v1=tr00v1.transformImage(imgD0, imgD0);
		ImagePlus imgD0to0_v2=tr00v2.transformImage(imgD0, imgD0);
	
		
		System.out.println("composite...");
		VitimageUtils.compositeNoAdjustOf(imgD0, imgD2to0_v1,"2to0v1").show();
		VitimageUtils.compositeNoAdjustOf(imgD0, imgD2to0_v2,"2to0v2").show();
	
		VitimageUtils.compositeNoAdjustOf(imgD2, imgD0to2_v1,"0to2v1").show();
		VitimageUtils.compositeNoAdjustOf(imgD2, imgD0to2_v2,"0to2v2").show();
		
		VitimageUtils.compositeNoAdjustOf(imgD0, imgD0to0_v1,"0to0v1").show();
		VitimageUtils.compositeNoAdjustOf(imgD0, imgD0to0_v2,"0to0v2").show();
	}

	
	public static void petitCacaToutMou() {
		ImagePlus imgRef=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D0_registered.tif");

		System.out.println("read...");
		String chComp="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/samples/transSubSpline01_80.mhd";
		ItkTransform tr=ItkTransform.readFromDenseFieldWithITKImporter(chComp);
		showAboutThisField(tr, imgRef, "01_80");

		System.out.println("read...");
		chComp="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/samples/transSubSpline01_116.mhd";
		tr=ItkTransform.readFromDenseFieldWithITKImporter(chComp);
		showAboutThisField(tr, imgRef, "01_116");

		System.out.println("read...");
		chComp="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/samples/transSubSpline01_116.mhd";
		tr=ItkTransform.readFromDenseFieldWithITKImporter(chComp);
		showAboutThisField(tr, imgRef, "12_0");
	}
	
	public static void tresGrosseChiasse() {
		System.out.println("read...");
		ImagePlus imgRef=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D0_registered.tif");
		String chComp="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/samples/transSubSpline01_0.mhd";
		System.out.println("read...");
		ItkTransform tr=ItkTransform.readFromDenseFieldWithITKImporter(chComp);
		showAboutThisField(tr, imgRef, "2_0_glob");

		String ch01="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_0_to_1.mhd";		
		String ch12="/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_1_to_2.mhd";
		
		System.out.println("read...");
		ItkTransform tr01=ItkTransform.readFromDenseFieldWithITKImporter(ch01);
		ItkTransform tr12=ItkTransform.readFromDenseFieldWithITKImporter(ch12);		
		ItkTransform tr02=ItkTransform.readFromDenseFieldWithITKImporter(ch01);
		System.out.println("add...");
		tr01.addTransform(tr12);
		tr01=tr01.flattenDenseField(imgRef);
		tr12=tr12.flattenDenseField(imgRef);
		System.out.println("show...");
		showAboutThisField(tr01, imgRef, "0_2_glob v01");
		showAboutThisField(tr, imgRef, "0_2_glob v02");

		System.out.println("invert...");
		ItkTransform tr20v1=tr01.getInverseOfDenseField();
		ItkTransform tr20v2=tr12.getInverseOfDenseField();
		System.out.println("show...");
		showAboutThisField(tr01, imgRef, "2_0_glob v01");
		showAboutThisField(tr, imgRef, "2_0_glob v02");
		
		
		//		ItkTransform tr0x=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/samples/transSubSpline12_60.mhd");
//		ItkTransform tr=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/samples/transSubSpline12_60.mhd");		
		
/*		ItkTransform tr00=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_0_to_0_global.mhd");
		tr00.showAsGrid3D(imgIn,6,"00",150);
		ItkTransform tr02=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_2_to_0_global.mhd");
		tr02.showAsGrid3D(imgIn,6,"02",150);

		ItkTransform tr03=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_3_to_0_global.mhd");
		tr03.showAsGrid3D(imgIn,6,"03",150);
	*/	
		
	}
	
	
	public static void grosseChiasse() {
		ImagePlus imgIn=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/img_interp/mushroom/seg12_33.tif");
		//imgIn=VitimageUtils.gaussianFiltering(imgIn, 0.1, 0.1, 0.1);
		imgIn.show();
		for(double sigma=0.013;sigma<0.02;sigma*=1.1) {
			System.out.println(sigma);
			ImagePlus img=new Duplicator().run(imgIn);
			IJ.run(img,"32-bit","");
			img=VitimageUtils.gaussianFiltering(img, sigma,sigma,sigma);
			img.setDisplayRange(0, 255);
			IJ.run(img,"8-bit","");
			img.show();
			img.setTitle("sigma="+sigma);
			IJ.run(img,"Fire","");
			img.setSlice(99);
		}
		VitimageUtils.waitFor(1000000);
	}
	
	public static void produceTestForSplineField() {
		ImagePlus img=ij.gui.NewImage.createImage("x",100,100,100,32,ij.gui.NewImage.FILL_BLACK);
		String rep="/home/fernandr/Bureau/Test/Spline/";
		IJ.run(img,"Multiply...","value=0 stack");
		IJ.saveAsTiff(img,rep+"val0.tif");
		ImagePlus img10=new Duplicator().run(img);
		IJ.run(img10,"Add...","value=10 stack");
		IJ.saveAsTiff(img10,rep+"val10.tif");
		ItkTransform tr1=new ItkTransform(new Transform(ItkImagePlusInterface.convertImagePlusArrayToDisplacementField(new ImagePlus[] {img10,img,img})));
		ItkTransform tr2=new ItkTransform(new Transform(ItkImagePlusInterface.convertImagePlusArrayToDisplacementField(new ImagePlus[] {img10,img10,img})));
		ItkTransform tr3=new ItkTransform(new Transform(ItkImagePlusInterface.convertImagePlusArrayToDisplacementField(new ImagePlus[] {img10,img10,img10})));
		tr1.writeAsDenseFieldWithITKExporter(rep+"tr1.transform");
		tr2.writeAsDenseFieldWithITKExporter(rep+"tr2.transform");
		tr3.writeAsDenseFieldWithITKExporter(rep+"tr3.transform");
//		tr1.writeAsDenseField(rep+"tr1.transform", img);
		//		tr2.writeAsDenseField(rep+"tr2.transform", img);
		//tr3.writeAsDenseField(rep+"tr3.transform", img);		
	}
	
	
	public static void produceIntermediary() {
		String rep="/home/fernandr/Bureau/Test/Spline/";
		ItkTransform tr1=ItkTransform.readFromDenseFieldWithITKImporter(rep+"tr1.mhd");
		ItkTransform tr2=ItkTransform.readFromDenseFieldWithITKImporter(rep+"tr2.mhd");
		ItkTransform tr3=ItkTransform.readFromDenseFieldWithITKImporter(rep+"tr3.mhd");
		boolean makeSplines=true;
		double intermTime;
		int N=3+1;
		int X=100;
		int Y=100;
		int Z=100;
		double voxSX=1;
		double voxSY=1;
		double voxSZ=1;
		ImagePlus imgTmpX=ij.gui.NewImage.createImage("x",100,100,100,32,ij.gui.NewImage.FILL_BLACK);
		ImagePlus imgTmpY=ij.gui.NewImage.createImage("y",100,100,100,32,ij.gui.NewImage.FILL_BLACK);
		ImagePlus imgTmpZ=ij.gui.NewImage.createImage("z",100,100,100,32,ij.gui.NewImage.FILL_BLACK);
		float[][]tabTmpX=new float[Z][];
		float[][]tabTmpY=new float[Z][];
		float[][]tabTmpZ=new float[Z][];
		for(int z=0;z<Z;z++) {
			tabTmpX[z]=(float[]) imgTmpX.getStack().getProcessor(z+1).getPixels();
			tabTmpY[z]=(float[]) imgTmpY.getStack().getProcessor(z+1).getPixels();
			tabTmpZ[z]=(float[]) imgTmpZ.getStack().getProcessor(z+1).getPixels();
		}
		
		
		Point3d p0;
		Point3d pT;
		int index;
		double[]dataT=new double[N];
		for(int t=0;t<N;t++)dataT[t]=t;

		System.out.println("Allocation des tableaux");
		double[][][][][]tabVals=new double[X][Y][Z][3][N];
		PolynomialSplineFunction[][][][]tabFunc=new PolynomialSplineFunction[X][Y][Z][3];
		System.out.println("Lecture et conservation des données initiales. Dimension tableau, en Megadoubles = "+VitimageUtils.dou(X*Y*Z*N*3/1000000.0));
		for(int x=0;x<X;x++) {
			System.out.println("x="+x+"/"+X);
			for(int y=0;y<Y;y++) {
				for(int z=0;z<Z;z++) {
					p0=new Point3d(x*voxSX,y*voxSY,z*voxSZ);
					pT=p0;
					tabVals[x][y][z][0][0]=pT.x-p0.x;
					tabVals[x][y][z][1][0]=pT.y-p0.y;
					tabVals[x][y][z][2][0]=pT.z-p0.z;

					pT=tr1.transformPoint(p0);
					tabVals[x][y][z][0][1]=pT.x-p0.x;
					tabVals[x][y][z][1][1]=pT.y-p0.y;
					tabVals[x][y][z][2][1]=pT.z-p0.z;

					pT=tr2.transformPoint(p0);
					tabVals[x][y][z][0][2]=pT.x-p0.x;
					tabVals[x][y][z][1][2]=pT.y-p0.y;
					tabVals[x][y][z][2][2]=pT.z-p0.z;

					pT=tr3.transformPoint(p0);
					tabVals[x][y][z][0][3]=pT.x-p0.x;
					tabVals[x][y][z][1][3]=pT.y-p0.y;
					tabVals[x][y][z][2][3]=pT.z-p0.z;
					
					
					if(makeSplines) {
						tabFunc[x][y][z][0]=new SplineInterpolator().interpolate(dataT,tabVals[x][y][z][0]);
						tabFunc[x][y][z][1]=new SplineInterpolator().interpolate(dataT,tabVals[x][y][z][1]);
						tabFunc[x][y][z][2]=new SplineInterpolator().interpolate(dataT,tabVals[x][y][z][2]);						
					}
					
				}	
			}			
		}
	
	
	
		ImagePlus [] tabConcX=new ImagePlus[3*30];
		ImagePlus []tabConcY=new ImagePlus[3*30];
		ImagePlus []tabConcZ=new ImagePlus[3*30];
		int incr=0;
		System.out.println("Construction des champs de vecteurs");
		for(int da=0;da<3;da++) {
			for(int interp=0;interp<30;interp++) {
				intermTime=da+interp/30.0;
				System.out.println("da="+da+" et interp="+interp+" --> intermTime="+intermTime);
				for(int x=0;x<X;x++) {
					for(int y=0;y<Y;y++) {
						index=x+X*y;
						for(int z=0;z<Z;z++) {
							tabTmpX[z][index]=(float)tabFunc[x][y][z][0].value(intermTime);
							tabTmpY[z][index]=(float)tabFunc[x][y][z][1].value(intermTime);
							tabTmpZ[z][index]=(float)tabFunc[x][y][z][2].value(intermTime);
						}
					}
				}
				IJ.saveAsTiff(imgTmpX,rep+"interp/tr"+da+"_"+interp+".x.tif");
				tabConcX[incr]=new Duplicator().run(imgTmpX);
				
				IJ.saveAsTiff(imgTmpY,rep+"interp/tr"+da+"_"+interp+".y.tif");
				tabConcY[incr]=new Duplicator().run(imgTmpY);

				IJ.saveAsTiff(imgTmpZ,rep+"interp/tr"+da+"_"+interp+".z.tif");
				tabConcZ[incr]=new Duplicator().run(imgTmpZ);
				
				incr++;
			}
		}
		ImagePlus imgFullX=Concatenator.run(tabConcX);
		imgFullX.show();
		imgFullX.setTitle("X");
		imgFullX.setDisplayRange(-2, 12);
		IJ.run(imgFullX,"Fire","");
		
		ImagePlus imgFullY=Concatenator.run(tabConcY);
		imgFullY.show();
		imgFullY.setTitle("Y");
		imgFullY.setDisplayRange(-2, 12);
		IJ.run(imgFullY,"Fire","");

		ImagePlus imgFullZ=Concatenator.run(tabConcZ);
		imgFullZ.show();
		imgFullZ.setTitle("Z");
		imgFullZ.setDisplayRange(-2, 12);
		IJ.run(imgFullZ,"Fire","");
		
		VitimageUtils.waitFor(1000000000);
	}	
	

	
	public static void testSpline0() {
		double[]dataT=new double[] {0,1,2,3};
		double[]dataValX=new double[] {0,3,3.5,4};
		PolynomialSplineFunction psf=new SplineInterpolator().interpolate(dataT,dataValX);
		System.out.print("X=[");
		for(double t=0;t<dataT.length-1;t+=0.1)System.out.print(" "+(t));
		System.out.println("]");
		System.out.print("Y=[");
		for(double t=0;t<dataT.length-1;t+=0.1)System.out.print(" "+psf.value(t));
		System.out.println("]");
		
	}
	
	
	public static void testGauss() {
		ImagePlus img=IJ.openImage("/home/fernandr/Bureau/Test/Visu/s010.tif");
		img.show();
		img.setSlice(241);
		double sigma=0.5;
		ImagePlus img2=VitimageUtils.gaussianFiltering(img, sigma, sigma, sigma);
		img2.show();
		img2.resetDisplayRange();
		img2.setSlice(241);
		VitimageUtils.waitFor(100000);
	}
	
	public static void testFields() {
		ImagePlus imgMovD0=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D0_registered.tif");
		ImagePlus imgMovD1=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D1_registered.tif");
		ImagePlus imgMovD2=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D2_registered.tif");
		ImagePlus imgMovD3=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D3_registered.tif");
		System.out.println("Here1");
		ItkTransform tr10=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_1_to_0_global.mhd");
		ItkTransform tr20=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_2_to_0_global.mhd");
		ItkTransform tr30=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_3_to_0_global.mhd");
		System.out.println("Here2");
		ImagePlus imgRec1=tr10.transformImage(imgMovD1,imgMovD1);
		ImagePlus imgRec2=tr20.transformImage(imgMovD2,imgMovD2);
		ImagePlus imgRec3=tr30.transformImage(imgMovD3,imgMovD3);

		imgMovD0.show();
		imgRec1.show();
		imgRec2.show();
		imgRec3.show();
		System.out.println("Here6");
		ImagePlus tout=Concatenator.run(new ImagePlus[] {imgMovD0,imgRec1,imgRec2,imgRec3 });
		tout.show();
		VitimageUtils.waitFor(1000000);
	}
	
	
	
	
	public static void testFields2() {
		ImagePlus imgMov=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/Img_intermediary/D0_registered.tif");
		System.out.println("Here1");
		ItkTransform tr01=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_0_to_1_sub.mhd");
		ItkTransform tr12=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_1_to_2_sub.mhd");
		ItkTransform tr23=ItkTransform.readFromDenseFieldWithITKImporter("/mnt/DD_COMMON/Data_VITIMAGE/Movie_maker_v2/champs/recalage_init/trans_2_to_3_sub.mhd");
		System.out.println("Here2");
		ItkTransform trFull=new ItkTransform();
		trFull.addTransform(tr01);
		trFull.addTransform(tr12);
		trFull.addTransform(tr23);
		System.out.println("Here3");
		ImagePlus imgTest1=trFull.transformImage(imgMov, imgMov);
		System.out.println("Here4");
		ImagePlus imgTest2=tr01.transformImage(imgMov, imgMov);
		System.out.println("Here5");
		imgTest2=tr12.transformImage(imgTest2,imgTest2);
		imgTest2=tr23.transformImage(imgTest2,imgTest2);
		System.out.println("Here6");
		imgTest1.show();
		imgTest2.show();
		VitimageUtils.waitFor(1000000);
	}
	
	
	public static void chiasse() {
		ImagePlus imgRef=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/Img_intermediary/D0_registered.tif");
		ImagePlus imgMov=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/Img_intermediary/D1_registered.tif");
		ItkTransform field=new ItkTransform(new DisplacementFieldTransform(
				ItkTransform.computeDenseFieldFromSparseCorrespondancePoints(VitiDialogs.registrationPointsUI(10, imgRef,imgRef ,true), imgRef, 0.35, true)));
		ImagePlus test=field.transformImage(imgRef,imgMov);
		System.out.println("ComputeDense ok.");
		
		ImagePlus img=field.viewAsGrid3D(imgRef, 7);
		img.show();
		test.show();
		System.out.println("Grid ok.");
		VitimageUtils.waitFor(100000);
		System.exit(0);
		System.out.println("Lecture...");
//		ImagePlus imgMov=IJ.openImage("/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/champs/recalage_init/d1_to_0.tif");
		double varMin=0.05;
		int duration=0;
		boolean displayRegistration=true;
		ImagePlus mask=IJ.openImage("/home/fernandr/Bureau/Test/Visu2/Compute/mask472.tif");
		boolean displayR2=false;
		int levMax =1;
		int levMin =0;
		int blockSize = 7;
		int neighSize = 3;
		double sigma  = 0.6 ;
		
		int dayI=0;
		int dayIPlus=dayI+1;		
		ItkTransform tr10=BlockMatchingRegistration.setupAndRunRoughBlockMatchingWithoutFineParameterization(imgRef, imgMov, mask,Transformation3DType.DENSE, MetricType.CORRELATION,
				levMax, levMin, blockSize, neighSize, varMin, sigma, duration, displayRegistration, displayR2,80,true);
		tr10.writeAsDenseFieldWithITKExporter("/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/champs/recalage_init/TEST_trans_"+dayIPlus+"_to_"+dayI+".mhd");
		System.out.print(" transform");
		ImagePlus res10=tr10.transformImage(imgRef, imgMov);
		IJ.saveAsTiff(res10, "/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/champs/recalage_init/TESTd"+dayIPlus+"_to_"+dayI+".tif");
		res10.show();
			
		VitimageUtils.waitFor(1000000);
		
	}
	
	public static void test3Fields() {
		long l1=System.currentTimeMillis();
		long  lStart=System.currentTimeMillis();
		l1=-lStart+System.currentTimeMillis();System.out.println("Start : "+l1);
		int dayMax=1;
		double varMin=0.025;
		int duration=0;
		boolean displayRegistration=false;
		ImagePlus mask=IJ.openImage("/home/fernandr/Bureau/Test/Visu2/Compute/mask512.tif");
		boolean displayR2=false;
		int levMax =2;
		int levMin =1;
		int blockSize = 3;
		int neighSize = 3;
		double sigma  = 0.7 ;
 
		l1=-lStart+System.currentTimeMillis();System.out.println("\nPhase 1 : registration forward. Start : "+(l1/1000.0));
		for(int dayI=0;dayI<dayMax;dayI++) {
			int dayIPlus=dayI+1;		
			System.out.print("Forward.  dayI="+dayI+"    open");
			ImagePlus imgRef=IJ.openImage("/home/fernandr/Bureau/Test/Visu2/Compute/D"+dayI+"_6_registered.tif");
			ImagePlus imgMov=IJ.openImage("/home/fernandr/Bureau/Test/Visu2/Compute/D"+dayIPlus+"_6_registered.tif");
			//imgRef=VitimageUtils.cropImageFloat(imgRef, 46*2, 55*2, 67*2, 160*2, 158*2, 128*2);
			//imgMov=VitimageUtils.cropImageFloat(imgMov, 46*2, 55*2, 67*2, 160*2, 158*2, 128*2);
			System.out.print(" sub");
			ImagePlus imgRefSub=VitimageUtils.Sub222(imgRef);
			ImagePlus imgMovSub=VitimageUtils.Sub222(imgMov);
			System.out.print(" bm ");
			ItkTransform tr10=BlockMatchingRegistration.setupAndRunRoughBlockMatchingWithoutFineParameterization(imgRefSub, imgMovSub, mask,Transformation3DType.DENSE, MetricType.CORRELATION,
					levMax, levMin, blockSize, neighSize, varMin, sigma, duration, displayRegistration, displayR2,80,false);
			System.out.print(" write");
			tr10.writeAsDenseFieldWithITKExporter("/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/champs/recalage_init/trans_"+dayIPlus+"_to_"+dayI+".mhd");
			System.out.print(" transform");
			ImagePlus res=tr10.transformImage(imgRef, imgMov);
			System.out.print(" save");
			IJ.saveAsTiff(res, "/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/champs/recalage_init/d"+dayIPlus+"_to_"+dayI+".tif");
		}
		l1=-lStart+System.currentTimeMillis();System.out.println("forward accomplished : "+(l1/1000.0));
		l1=-lStart+System.currentTimeMillis();System.out.println("\nPhase 2 : registration backwards. Start : "+(l1/1000.0));
		for(int dayI=0;dayI<dayMax;dayI++) {
			System.out.print("start");
			int dayIPlus=dayI+1;		
			System.out.print("Backwards. dayI="+dayI+"    open");
			ImagePlus imgMov=IJ.openImage("/home/fernandr/Bureau/Test/Visu2/Compute/D"+dayI+"_6_registered.tif");
			ImagePlus imgRef=IJ.openImage("/home/fernandr/Bureau/Test/Visu2/Compute/D"+dayIPlus+"_6_registered.tif");
			//imgRef=VitimageUtils.cropImageFloat(imgRef, 46*2, 55*2, 67*2, 160*2, 158*2, 128*2);
			//imgMov=VitimageUtils.cropImageFloat(imgMov, 46*2, 55*2, 67*2, 160*2, 158*2, 128*2);
			System.out.print(" sub");
			ImagePlus imgRefSub=VitimageUtils.Sub222(imgRef);
			ImagePlus imgMovSub=VitimageUtils.Sub222(imgMov); 
			System.out.print(" bm");
			ItkTransform tr01=BlockMatchingRegistration.setupAndRunRoughBlockMatchingWithoutFineParameterization(imgRefSub, imgMovSub, mask,Transformation3DType.DENSE, MetricType.CORRELATION,
					levMax, levMin, blockSize, neighSize, varMin, sigma, duration, displayRegistration, displayR2,80,false);
			System.out.print(" write");
			tr01.writeAsDenseFieldWithITKExporter("/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/champs/recalage_init/trans_"+dayI+"_to_"+dayIPlus+".mhd");
			System.out.print(" transform");
			ImagePlus res=tr01.transformImage(imgRef, imgMov);
			System.out.print(" save");
			IJ.saveAsTiff(res, "/mnt/DD_COMMON/Data_VITIMAGE/Visu2_keep/champs/recalage_init/d"+dayI+"_to_"+dayIPlus+".tif");
		}
		l1=-lStart+System.currentTimeMillis();System.out.println("backwards accomplished : "+(l1/1000.0));
	}
	
	
	public static void test4Fields() {

	}
	


	
	public static void test() {

		ImagePlus img1=IJ.openImage("/home/fernandr/Bureau/Test/Test_NN/test_IRM-1.tif");
		ImagePlus img2=IJ.openImage("/home/fernandr/Bureau/Test/Test_NN/test_IRM.tif");
		IJ.run(img1,"32-bit","");
		IJ.run(img2,"32-bit","");
		img1.show();
//		img2.show();
		VitimageUtils.waitFor(10000);
		RoiManager rm=RoiManager.getRoiManager();
		Roi roi=rm.getRoi(0);
		
		rm.remove(0);
		rm.close();
		img1.changes=false;
		img1.close();
		 img1=IJ.openImage("/home/fernandr/Bureau/Test/Test_NN/test_IRM-1.tif");
		 img1.show();
		 rm=RoiManager.getRoiManager();
		System.out.println("Retrait effectué");
		VitimageUtils.waitFor(5000);
		System.out.println("Chargement equivalent");
		rm.setVisible(true);
		rm.show();
		VitimageUtils.waitFor(50000);
	}
	
	
	/** Input of the plugin :    - ImageJ is opened, with NImg 2-D images opened : M0, T1, T2, RX, ....
	 *  						 - The ROI manager is opened, with Nroi ROIs : bois_noir,bois_pasnoir, bois_pasdutoutnoir, ... 
	 *  						 
	*/	
	public void run(String arg) {
		
		//Script parameters (feel free to change them)
		boolean chooseCustomDirectory=false;
		boolean testRomain=true;	
		String pathOnCedricComputer="D:\\TRAVAIL\\Manip\\VITIMAGE_Imagerie\\ANALYSE DONNEES\\2019-01_Recalage_CEP002_to_photograph_31_mu\\mesures sur ROI\\data\\blabla";
		String pathOnRomainComputer="/home/fernandr/Bureau/Test/TestRoiCedric";
		int nbMinOfImages=1;
		int nbMinOfRois=1;		
		//Edit from last version : Have to import this : import java.awt.Point;

		
		//Setup for output path
		String stDir =System.getProperty("user.dir");
		String path="";
		if(chooseCustomDirectory) {
			OpenDialog od=new OpenDialog("Choose an output path");
			path=od.getDirectory();
		}
		else{
			if(testRomain)path=pathOnRomainComputer;
			else path=pathOnCedricComputer;
		}		

		
		//Setup information to user
		IJ.log("Setup summary :");
		IJ.log("-- Current dir : "+stDir);
		IJ.log("-- Output dir  : "+path);
		

		
		//  Step 1 : Identify images and get their titles
		IJ.log("\nImage detection");
		String imgName;
		int[] wList = WindowManager.getIDList();
        if (wList==null) {IJ.error("No images are open.");return;}
        String[] imgTitles = new String[wList.length];
        ImagePlus[]imgs=new ImagePlus[wList.length];
		int yMax=WindowManager.getImage(wList[0]).getHeight();
		int xMax=WindowManager.getImage(wList[0]).getWidth();
        for(int indImg=0;indImg<wList.length;indImg++){
			imgTitles[indImg]=WindowManager.getImage(wList[indImg]).getShortTitle();
			IJ.log(" - Image numero "+indImg+" : "+imgTitles[indImg]);
			imgs[indImg]=WindowManager.getImage(wList[indImg]);
			WindowManager.getImage(wList[indImg]).hide();
        }
		if(wList.length < nbMinOfImages){IJ.error("Not enough opened images : "+wList.length);return;}
		int nbImg=wList.length;
		

		//Step 2 : Identify Rois and get their titles
		IJ.log("\nROIs detection");
        String roiName;
		RoiManager roiMg=RoiManager.getRoiManager();
        if (roiMg==null) {IJ.error("No ROI opened : ");return;}
		int nbRoi=roiMg.getCount(); 								// compte le nb de ROI dans manager
        String[] roiTitles = new String[nbRoi];
        for(int indRoi=0;indRoi<nbRoi;indRoi++){
			roiTitles[indRoi]=roiMg.getName(indRoi);
			IJ.log(" - Roi numero "+indRoi+" : "+roiTitles[indRoi]);
        }
		if(nbRoi < nbMinOfRois){IJ.error("Not enough ROIs : "+nbRoi);return;}  // défini le nb de ROI minimal
		
		

		//--------------------------------------
		// For each couple (Image , ROI ), export the target pixels in two structured formats :
		// --> rows vectors (Xpixel,Ypixel,VALUEpixel) in a excel-style file ROI_DATA_nameoftheroi_nameofthemodality.csv  
		// --> Masked image, in a image file ROI_DATA_nameoftheroi_nameofthemodality.tif
		//

		
		IJ.log("\n\nProcessing Data");
		int nMatch;
		File f = new File(path+((testRomain) ? "/" : "\\")+"pixel_data.csv");
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));		
	
			
			
			//For each roi
			for(int indRoi=0;indRoi<nbRoi;indRoi++){
				Roi currentRoi=roiMg.getRoi(indRoi);
				
				
				//for each image
				for(int indImg=0;indImg<nbImg;indImg++){
					imgs[indImg].show();
					System.out.println(indImg+","+indRoi);
   		            IJ.log("Processing Image-"+indImg+" ("+imgTitles[indImg]+")   X   Roi-"+indRoi+" ("+roiTitles[indRoi]+")");
					nMatch=0;
	
					//Open the image
					roiMg.runCommand(WindowManager.getImage(wList[indImg]),"Show All");
					roiMg.select(indRoi);
						
					byte[] pixelsIn=(byte[])(imgs[indImg].getStack().getProcessor(1).getPixels());
					ByteProcessor maskProc = imgs[indImg].createRoiMask();
					byte[] mask=(byte[])maskProc.getPixels();
	
					ImagePlus imPlusOut=ij.gui.NewImage.createImage("img_ROI_"+roiTitles[indRoi]+"_"+imgTitles[indImg]+".tif",xMax,yMax,1,8,ij.gui.NewImage.FILL_BLACK);  // cree image vide pour y ajouter les pixels de la ROI
					byte[] pixelsOut=(byte[])(imPlusOut).getProcessor().getPixels();
					for(int xx=0;xx<xMax;xx++){
						for(int yy=0;yy<yMax;yy++){
							if(mask[xMax*yy+xx]==(byte)( 255)) {
								nMatch++;
								pixelsOut[xMax*yy+xx]=pixelsIn[xMax*yy+xx];//copy it in a new image
								out.write(xx+" "+yy+" "+(int)(pixelsIn[xMax*yy+xx] & 0xff)+" "+roiTitles[indRoi]+" "+imgTitles[indImg]+"\n");//write it to csv file
							}
						}
					}
					IJ.log(" -> Number of pixels in this area = "+nMatch);
	
	//				imPlusOut.show(); //Uncomment this line to open the successive sub-images 
					FileSaver fs=new FileSaver(imPlusOut);
					String s="img_ROI_"+roiTitles[indRoi]+"_"+imgTitles[indImg]+".tif";
					fs.saveAsTiff(path+((testRomain) ? "/" : "\\")+s);
	
					imgs[indImg].hide();
				}
			}
			out.close();
		} catch (Exception e) {	IJ.log("Here is a problem");		}

		// Step 3 : suggest that user can play with joint histogram and get fun
		IJ.log("Use the joint histogram routine in order to explore the link between modalities over the rois");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void testCorr() {
		int nTest=50000;
		int nCoefs=20;
		int coef1=3;
		int cofTmp;
		double[]vecta=vectRand(10,1,100,100,20);
		double[]vectb=vectRand(5,3,100,100,20);
		double [][]tabVals=new double[nCoefs][nTest];
		double []means=new double[nCoefs];
		double []stds=new double[nCoefs];
		double[]result;
		for(int nc=0;nc<nCoefs;nc++) {
			cofTmp=coef1+5*nc;
			for(int nt=0;nt<nTest;nt++) {
				vecta=vectRand(10,1,cofTmp,cofTmp,2);
				vectb=vectRand(5,3,cofTmp,cofTmp,2);
				tabVals[nc][nt]=correlationCoefficient(vecta, vectb);
				if((nc+nt)==0) {
					System.out.println("Une verif");
					System.out.println(TransformUtils.stringVectorN(vecta,"vecta"));
					System.out.println(TransformUtils.stringVectorN(vectb,"vectb"));
					System.out.println("corr="+tabVals[nc][nt]);
				}
			}
			result=VitimageUtils.statistics1D(tabVals[nc]);
			means[nc]=result[0];
			stds[nc]=result[1];
			System.out.println("Avec nb="+cofTmp+" : mean="+means[nc]+"  ,  std="+stds[nc]);
		}		
	}

	public static double correlationCoefficient(double X[], double Y[]) { 
		//System.out.println("En effet, X.length="+X.length);
		//System.out.println("En effet, Y.length="+Y.length);
		double epsilon=10E-20;
		if(X.length !=Y.length )IJ.log("In correlationCoefficient in BlockMatching, blocks length does not match");
		int n=X.length;
		double sum_X = 0, sum_Y = 0, sum_XY = 0; 
		double squareSum_X = 0, squareSum_Y = 0; 	
		for (int i = 0; i < n; i++) { 
			sum_X = sum_X + X[i]; 		
			sum_Y = sum_Y + Y[i]; 
			sum_XY = sum_XY + X[i] * Y[i]; 
			squareSum_X = squareSum_X + X[i] * X[i]; 
			squareSum_Y = squareSum_Y + Y[i] * Y[i]; 
		} 
		if(squareSum_X<epsilon || squareSum_Y<epsilon )return 0;
		// use formula for calculating correlation  
		// coefficient. 
		return (  (n * sum_XY - sum_X * sum_Y)/ (Math.sqrt((n * squareSum_X - sum_X * sum_X) * (n * squareSum_Y - sum_Y * sum_Y)))); 
	} 
	
	public static double[]vectRand(int a, int b, int N,int n,double sigma){
		Random rand=new Random();
		double[]vect=new double[n];
		for(int i=0;i<n;i++) {
			vect[i]=VitimageUtils.dou(a*i*1.0/N+b+rand.nextDouble()*sigma);
		}
		return vect;
	}
	
	
	
	
	public static int[][]getTestTabRXPhoto(){
		return new int[][]{
			{0,0,0,0},
			{4,4,4,4},
			{5,5,5,5},
			{10,10,10,10},
			{11,11,11,11},
			{16,16,16,15},
			{15,16,17,18},
			{21,22,23,23},
			{22,23,24,25},
			{27,28,29,30},
			{27,28,27,27},
			{30,30,30,30},
			{31,30,30,30},
			{38,38,39,39},
			{39,39,39,39},
			{41,40,39,39},
			{42,42,41,41},
			{47,47,47,47},
			{48,47,47,47},
			{54,54,53,52},
			{54,53,52,52},
			{59,59,58,57},
			{59,59,58,58},
			{64,63,62,61},
			{65,65,64,64},
			{70,70,70,70},
			{71,71,70,70},
			{77,77,76,76},
			{78,78,77,77},
			{83,82,81,80},
			{84,83,83,82},
			{91,90,89,88},
			{91,90,89,88},
			{96,95,94,93},
			{96,95,94,93},
			{101,101,101,101},
			{102,101,101,101},
			{107,108,107,106},
			{107,107,107,108},
			{112,112,111,110},
			{113,112,112,112},
			{118,117,117,118},
			{118,118,118,117},
			{121,121,121,122},
			{124,124,124,123},
			{127,128,128,127},
			{127,128,127,127},
			{132,133,133,133},
			{133,134,134,134},
			{140,141,140,140},
			{140,140,140,140},
			{148,149,148,148},
			{148,148,148,148},
			{159,158,157,156},
			{161,160,160,161},
			{163,163,162,162},
			{165,165,165,165},
			{168,169,170,171},
			{171,172,172,172},
			{174,175,174,174},
			{177,178,178,179},
			{180,180,180,180},
			{181,180,181,181},
			{187,188,188,189},
			{188,188,189,189},
			{190,190,190,190},
			{191,190,189,189},
			{199,200,200,200},
			{199,200,200,199},
			{204,204,203,203},
			{204,204,204,204},
			{211,211,212,213},
			{211,211,211,210},
			{217,218,217,218},
			{217,217,217,216},
			{221,222,221,221},
			{221,222,221,221},
			{226,226,227,227},
			{225,225,225,224},
			{231,232,233,234},
			{232,233,234,234},
			{237,237,236,235},
			{239,240,240,239},
			{245,246,247,247},
			{246,247,247,247},
			{250,249,250,250},
			{252,253,254,255},
			{256,256,255,256},
			{257,257,256,255},
			{261,260,259,258},
			{262,261,260,260},
			{267,266,265,265},
			{268,267,266,265},
			{272,271,270,269},
			{273,272,271,270},
			{278,277,276,277},
			{281,282,282,283},
			{284,285,286,287},
			{286,285,284,283},
			{286,286,286,287},
			{287,286,285,284},
			{292,293,293,294},
			{293,294,294,293},
			{299,300,300,299},
			{298,297,296,295},
			{303,302,301,300},
			{304,303,303,302},
			{310,309,308,307},
			{313,314,313,314},
			{316,315,314,314},
			{317,318,318,317},
			{324,324,323,324},
			{325,326,327,327},
			{330,330,331,331},
			{330,330,331,331},
			{334,334,334,333},
			{337,338,337,337},
			{342,343,344,344},
			{342,343,343,344},
			{349,350,350,350},
			{348,348,348,348},
			{353,353,354,355},
			{355,356,355,355},
			{361,362,363,362},
			{362,361,361,362},
			{367,367,367,367},
			{367,367,366,366},
			{372,373,372,372},
			{374,373,372,372},
			{380,381,380,380},
			{380,380,380,380},
			{386,387,387,387},
			{387,388,389,389},
			{390,391,390,390},
			{393,394,394,394},
			{397,398,397,397},
			{400,401,400,400},
			{403,402,401,401},
			{406,405,404,403},
			{411,411,410,410},
			{411,411,411,411},
			{419,420,419,419},
			{419,419,419,419},
			{425,426,426,426},
			{425,426,427,427},
			{430,430,430,429},
			{433,434,434,434},
			{437,437,436,435}
		};
	}
	
	
	
	
	
	public TestRomain() {
		// TODO Auto-generated constructor stub
	}

}
