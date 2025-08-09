import io.github.dhruv1110.jcachex.spring.annotations.JCacheXCacheable;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ComputationService {

    @JCacheXCacheable(cacheName = "fibonacciResults", key = "#n", expireAfterWrite = 60, expireAfterWriteUnit = TimeUnit.MINUTES, maximumSize = 1000)
    public BigInteger fibonacci(int n) {
        if (n <= 1) {
            return BigInteger.valueOf(n);
        }

        BigInteger prev = BigInteger.ZERO;
        BigInteger curr = BigInteger.ONE;

        for (int i = 2; i <= n; i++) {
            BigInteger next = prev.add(curr);
            prev = curr;
            curr = next;
        }

        return curr;
    }

    @JCacheXCacheable(cacheName = "primeFactors", key = "#number", expireAfterWrite = 30, expireAfterWriteUnit = TimeUnit.MINUTES)
    public List<Long> getPrimeFactors(long number) {
        List<Long> factors = new ArrayList<>();

        for (long i = 2; i * i <= number; i++) {
            while (number % i == 0) {
                factors.add(i);
                number /= i;
            }
        }

        if (number > 1) {
            factors.add(number);
        }

        return factors;
    }

    @JCacheXCacheable(cacheName = "reportData", key = "#startDate + '-' + #endDate + '-' + #reportType", expireAfterWrite = 2, expireAfterWriteUnit = TimeUnit.HOURS)
    public ReportData generateReport(LocalDate startDate, LocalDate endDate, String reportType) {
        // Simulate heavy report generation
        return switch (reportType) {
            case "SALES" -> generateSalesReport(startDate, endDate);
            case "ANALYTICS" -> generateAnalyticsReport(startDate, endDate);
            case "PERFORMANCE" -> generatePerformanceReport(startDate, endDate);
            default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
        };
    }

    @Async
    @JCacheXCacheable(cacheName = "asyncComputations", key = "#taskId", expireAfterWrite = 45, expireAfterWriteUnit = TimeUnit.MINUTES)
    public CompletableFuture<ComputationResult> performAsyncComputation(String taskId, ComputationParameters params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate heavy computation
                Thread.sleep(5000);

                double result = switch (params.getType()) {
                    case "MONTE_CARLO" -> performMonteCarloSimulation(params);
                    case "MATRIX_OPERATIONS" -> performMatrixOperations(params);
                    case "DATA_ANALYSIS" -> performDataAnalysis(params);
                    default -> 0.0;
                };

                return new ComputationResult(taskId, result, LocalDateTime.now());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Computation interrupted", e);
            }
        });
    }

    @JCacheXCacheable(cacheName = "machineLearningPredictions", key = "#modelId + '-' + #inputData.hashCode()", expireAfterWrite = 15, expireAfterWriteUnit = TimeUnit.MINUTES)
    public PredictionResult predict(String modelId, InputData inputData) {
        // Simulate ML model prediction
        try {
            Thread.sleep(2000); // Simulate model inference time

            double prediction = performPrediction(modelId, inputData);
            double confidence = calculateConfidence(prediction, inputData);

            return new PredictionResult(
                    modelId,
                    prediction,
                    confidence,
                    LocalDateTime.now());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Prediction interrupted", e);
        }
    }

    private ReportData generateSalesReport(LocalDate startDate, LocalDate endDate) {
        // Simulate heavy sales report generation
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return new ReportData(
                "SALES",
                Map.of(
                        "totalSales", BigDecimal.valueOf(1000000),
                        "totalOrders", BigDecimal.valueOf(5000),
                        "averageOrderValue", BigDecimal.valueOf(200)),
                startDate,
                endDate);
    }

    private ReportData generateAnalyticsReport(LocalDate startDate, LocalDate endDate) {
        // Simulate heavy analytics report generation
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return new ReportData(
                "ANALYTICS",
                Map.of(
                        "pageViews", BigDecimal.valueOf(500000),
                        "uniqueVisitors", BigDecimal.valueOf(25000),
                        "bounceRate", BigDecimal.valueOf(0.35)),
                startDate,
                endDate);
    }

    private ReportData generatePerformanceReport(LocalDate startDate, LocalDate endDate) {
        // Simulate heavy performance report generation
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return new ReportData(
                "PERFORMANCE",
                Map.of(
                        "averageResponseTime", BigDecimal.valueOf(150),
                        "errorRate", BigDecimal.valueOf(0.02),
                        "throughput", BigDecimal.valueOf(1000)),
                startDate,
                endDate);
    }

    private double performMonteCarloSimulation(ComputationParameters params) {
        // Simulate Monte Carlo simulation
        Random random = new Random();
        int iterations = params.getIterations();
        int insideCircle = 0;

        for (int i = 0; i < iterations; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();

            if (x * x + y * y <= 1) {
                insideCircle++;
            }
        }

        return 4.0 * insideCircle / iterations; // Approximation of π
    }

    private double performMatrixOperations(ComputationParameters params) {
        // Simulate matrix operations
        int size = params.getMatrixSize();
        double[][] matrix = new double[size][size];

        // Initialize matrix with random values
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextDouble();
            }
        }

        // Perform matrix operations (determinant calculation)
        return calculateDeterminant(matrix);
    }

    private double performDataAnalysis(ComputationParameters params) {
        // Simulate data analysis
        List<Double> data = generateRandomData(params.getDataSize());

        // Calculate statistics
        double mean = data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = data.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance); // Standard deviation
    }

    private double performPrediction(String modelId, InputData inputData) {
        // Simulate ML prediction
        return inputData.getFeatures().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * 1.5; // Simple prediction logic
    }

    private double calculateConfidence(double prediction, InputData inputData) {
        // Simulate confidence calculation
        return Math.min(0.95, 0.7 + Math.random() * 0.25);
    }

    private double calculateDeterminant(double[][] matrix) {
        // Simple determinant calculation for demonstration
        int n = matrix.length;
        if (n == 1)
            return matrix[0][0];
        if (n == 2)
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

        double det = 0;
        for (int i = 0; i < n; i++) {
            det += Math.pow(-1, i) * matrix[0][i] * calculateDeterminant(getSubMatrix(matrix, 0, i));
        }
        return det;
    }

    private double[][] getSubMatrix(double[][] matrix, int excludeRow, int excludeCol) {
        int n = matrix.length;
        double[][] subMatrix = new double[n - 1][n - 1];
        int row = 0;

        for (int i = 0; i < n; i++) {
            if (i == excludeRow)
                continue;
            int col = 0;
            for (int j = 0; j < n; j++) {
                if (j == excludeCol)
                    continue;
                subMatrix[row][col] = matrix[i][j];
                col++;
            }
            row++;
        }
        return subMatrix;
    }

    private List<Double> generateRandomData(int size) {
        Random random = new Random();
        return IntStream.range(0, size)
                .mapToDouble(i -> random.nextGaussian())
                .boxed()
                .collect(Collectors.toList());
    }
}
