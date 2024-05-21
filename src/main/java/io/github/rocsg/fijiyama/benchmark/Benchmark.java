package io.github.rocsg.fijiyama.benchmark;

import ij.IJ;
import ij.ImagePlus;
import io.github.rocsg.fijiyama.common.ItkImagePlusInterface;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.rocsg.fijiyama.common.ConverteritkPlus.*;

public class Benchmark {

    private static final int NUM_REPETITIONS = 5; // Nombre de répétitions pour chaque conversion

    double avgDirectVoxelPerTime;
    double avgStreamVoxelPerTime;
    double avg_Old_MethodVoxelPerTime;

    public static int numVoxelsInImage(ImagePlus image) {
        return image.getWidth() * image.getHeight() * image.getStackSize();
    }
    public void runBenchmarks() {
        List<BenchmarkResult> results = new ArrayList<>();

        String numB ="all";
        String folderPath = "C:\\Users\\loaiu\\Documents\\Etudes\\MAM\\MAM5\\Stage\\data\\Nouveau_dossier\\Test\\" + numB + "\\";

        try {
            Files.list(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        BenchmarkResult result = benchmarkImage(file);
                        results.add(result);
                        try (FileWriter writer = new FileWriter("benchmark_results - perVoxel.csv", true)) {
                            writer.append(String.format("%s,%.5e,%.5e,%.5e\n", file.toString(), avgDirectVoxelPerTime, avgStreamVoxelPerTime, avg_Old_MethodVoxelPerTime));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        generateCSV(results, "benchmark_results - " + numB + ".csv");
    }

    private BenchmarkResult benchmarkImage(Path imagePath) {
        double totalDirectTime = 0;
        double totalStreamTime = 0;
        double total_Old_Method = 0;

        ImagePlus imagePlus = IJ.openImage(imagePath.toString());

        for (int i = 0; i < NUM_REPETITIONS; i++) {
            long startTime = System.nanoTime();
            ImagePlus directResult = convertITK2ImagePlusDirect(convertImagePlus2ImageITK(imagePlus));
            long endTime = System.nanoTime();
            totalDirectTime += TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            startTime = System.nanoTime();
            ImagePlus streamResult = convertITK2ImagePlusDirectST(convertImagePlus2ImageITKST(imagePlus));
            endTime = System.nanoTime();
            totalStreamTime += TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            startTime = System.nanoTime();
            ImagePlus oldResult = ItkImagePlusInterface.itkImageToImagePlusNew(ItkImagePlusInterface.imagePlusToItkImageNew(imagePlus));
            endTime = System.nanoTime();
            total_Old_Method += TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        }

        double avgDirectTime = totalDirectTime / NUM_REPETITIONS;
        double avgStreamTime = totalStreamTime / NUM_REPETITIONS;
        double avg_Old_Method = total_Old_Method / NUM_REPETITIONS;

        avgDirectVoxelPerTime = numVoxelsInImage(imagePlus) / avgDirectTime*1000;
        avgStreamVoxelPerTime = numVoxelsInImage(imagePlus) / avgStreamTime*1000;
        avg_Old_MethodVoxelPerTime = numVoxelsInImage(imagePlus) / avg_Old_Method*1000;

        return new BenchmarkResult(imagePath.toString(), avgDirectTime, avgStreamTime, avg_Old_Method);
    }

    private void generateCSV(List<BenchmarkResult> results, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Image Path, Direct Conversion Time (ms), Stream Conversion Time (ms), Old Method Time (ms)\n");

            for (BenchmarkResult result : results) {
                writer.append(String.format("%s,%.3f,%.3f,%.3f\n", result.getImagePath(), result.getDirectTime(), result.getStreamTime(), result.getOldMethod()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class BenchmarkResult {
        private final String imagePath;
        private final double directTime;
        private final double streamTime;
        private final double oldMethod;

        public BenchmarkResult(String imagePath, double directTime, double streamTime, double oldMethod) {
            this.imagePath = imagePath;
            this.directTime = directTime;
            this.streamTime = streamTime;
            this.oldMethod = oldMethod;
        }

        public String getImagePath() {
            return imagePath;
        }

        public double getDirectTime() {
            return directTime;
        }

        public double getStreamTime() {
            return streamTime;
        }

        public double getOldMethod() {
            return oldMethod;
        }
    }

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();
        benchmark.runBenchmarks();
    }
}