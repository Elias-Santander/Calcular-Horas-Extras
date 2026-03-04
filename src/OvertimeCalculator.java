import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Programa de consola para calcular el valor y total de horas extras de un empleado.
 *
 * <p>Solicita al usuario los datos necesarios (nombre, salario mensual, horas trabajadas
 * y horas ordinarias contractuales) y calcula el valor de la hora extra y el monto total
 * a pagar por dichas horas.</p>
 *
 * @author <a href="https://github.com/Elias-Santander">Elias Santander</a>
 * @version 1.0
 */
public class OvertimeCalculator {

    // -----------------------------------------------------------------------
    // Constantes de negocio
    // -----------------------------------------------------------------------

    /** Número de días laborables en un mes estándar. */
    private static final int WORKING_DAYS_PER_MONTH = 30;

    /** Número de horas laborables en un día estándar. */
    private static final int WORKING_HOURS_PER_DAY = 8;

    /** Factor de recargo aplicado sobre el valor de la hora ordinaria para calcular la hora extra. */
    private static final double OVERTIME_SURCHARGE_FACTOR = 1.5;

    // -----------------------------------------------------------------------
    // Punto de entrada
    // -----------------------------------------------------------------------

    /**
     * Método principal que ejecuta el flujo completo del programa.
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {

        // Iniciamos el scanner para leer entrada del usuario
        try (Scanner scanner = new Scanner(System.in)) {

            printHeader();

            // --- Recolección de datos ---
            String employeeName   = readEmployeeName(scanner);
            double monthlySalary  = readPositiveDouble(scanner, "Ingrese el salario mensual bruto ($): ");
            double hoursWorked    = readPositiveDouble(scanner, "Ingrese el total de horas trabajadas este mes: ");
            double contractHours  = readContractHours(scanner);

            // --- Cálculo ---
            OvertimeResult result = calculateOvertime(monthlySalary, hoursWorked, contractHours);

            // --- Reporte ---
            printReport(employeeName, monthlySalary, hoursWorked, contractHours, result);

        } catch (Exception e) {
            // Captura cualquier excepción inesperada y muestra mensaje amigable
            System.err.println("\n[ERROR] Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Métodos de lectura / validación
    // -----------------------------------------------------------------------

    /**
     * Imprime el encabezado decorativo del programa.
     */
    private static void printHeader() {
        System.out.println("=".repeat(55));
        System.out.println("       CALCULADORA DE HORAS EXTRAS");
        System.out.println("=".repeat(55));
    }

    /**
     * Lee y valida el nombre del empleado (no puede estar vacío).
     *
     * @param scanner instancia de {@link Scanner} para leer la entrada
     * @return nombre del empleado ingresado por el usuario
     */
    private static String readEmployeeName(Scanner scanner) {
        String name;
        while (true) {
            System.out.print("Ingrese el nombre del empleado: ");
            name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                return name;
            }
            // Validación: el nombre no puede estar vacío
            System.out.println("  [!] El nombre no puede estar vacío. Intente nuevamente.");
        }
    }

    /**
     * Lee un número decimal positivo desde la consola, reintentando si el valor
     * ingresado no es válido o es menor o igual a cero.
     *
     * @param scanner instancia de {@link Scanner} para leer la entrada
     * @param prompt  mensaje que se muestra al usuario antes de leer el valor
     * @return valor decimal positivo ingresado por el usuario
     */
    private static double readPositiveDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = scanner.nextDouble();
                scanner.nextLine(); // consumir el salto de línea pendiente
                if (value > 0) {
                    return value;
                }
                // Validación: el valor debe ser mayor que cero
                System.out.println("  [!] El valor debe ser mayor que cero. Intente nuevamente.");
            } catch (InputMismatchException e) {
                // El usuario ingresó algo que no es un número
                System.out.println("  [!] Entrada inválida. Por favor ingrese un número válido.");
                scanner.nextLine(); // limpiar buffer para evitar bucle infinito
            }
        }
    }

    /**
     * Lee las horas contractuales ordinarias del mes, ofreciendo un valor
     * por defecto calculado a partir de {@code WORKING_DAYS_PER_MONTH} y
     * {@code WORKING_HOURS_PER_DAY}.
     *
     * @param scanner instancia de {@link Scanner} para leer la entrada
     * @return total de horas ordinarias pactadas en el contrato
     */
    private static double readContractHours(Scanner scanner) {
        // Calculamos el valor por defecto (ej: 30 días × 8 horas = 240)
        double defaultHours = (double) WORKING_DAYS_PER_MONTH * WORKING_HOURS_PER_DAY;
        System.out.printf("Ingrese las horas ordinarias contractuales del mes [%.0f]: ", defaultHours);

        String input = scanner.nextLine().trim();

        // Si el usuario presiona Enter sin ingresar nada, usamos el valor por defecto
        if (input.isEmpty()) {
            System.out.printf("  [i] Se usará el valor por defecto: %.0f horas.%n", defaultHours);
            return defaultHours;
        }

        // Si ingresó un valor, lo validamos
        try {
            double value = Double.parseDouble(input);
            if (value > 0) {
                return value;
            }
            System.out.println("  [!] Valor inválido. Se usará el valor por defecto.");
        } catch (NumberFormatException e) {
            // El usuario ingresó texto no numérico; usamos el valor por defecto
            System.out.println("  [!] Entrada inválida. Se usará el valor por defecto.");
        }
        return defaultHours;
    }

    // -----------------------------------------------------------------------
    // Lógica de negocio
    // -----------------------------------------------------------------------

    /**
     * Calcula el valor de la hora ordinaria, el valor de la hora extra y
     * el monto total a pagar por las horas extras trabajadas.
     *
     * <p>Fórmulas utilizadas:</p>
     * <ul>
     *   <li>Hora ordinaria  = salario mensual / horas contractuales</li>
     *   <li>Hora extra      = hora ordinaria × {@value #OVERTIME_SURCHARGE_FACTOR}</li>
     *   <li>Horas extra     = horas trabajadas − horas contractuales (mínimo 0)</li>
     *   <li>Total horas extra = horas extra × valor hora extra</li>
     * </ul>
     *
     * @param monthlySalary  salario mensual bruto del empleado
     * @param hoursWorked    total de horas efectivamente trabajadas en el mes
     * @param contractHours  horas ordinarias pactadas en el contrato para el mes
     * @return objeto {@link OvertimeResult} con todos los valores calculados
     */
    private static OvertimeResult calculateOvertime(double monthlySalary,
                                                    double hoursWorked,
                                                    double contractHours) {
        // Valor de la hora ordinaria
        double ordinaryHourlyRate = monthlySalary / contractHours;

        // Valor de la hora extra con recargo
        double overtimeHourlyRate = ordinaryHourlyRate * OVERTIME_SURCHARGE_FACTOR;

        // Horas extras (no puede ser negativo si el empleado no superó el límite)
        double overtimeHours = Math.max(0, hoursWorked - contractHours);

        // Monto total a pagar por horas extras
        double totalOvertimePay = overtimeHours * overtimeHourlyRate;

        return new OvertimeResult(ordinaryHourlyRate, overtimeHourlyRate, overtimeHours, totalOvertimePay);
    }

    // -----------------------------------------------------------------------
    // Reporte
    // -----------------------------------------------------------------------

    /**
     * Imprime el reporte detallado de horas extras en consola.
     *
     * @param employeeName   nombre del empleado
     * @param monthlySalary  salario mensual bruto
     * @param hoursWorked    horas trabajadas en el mes
     * @param contractHours  horas ordinarias contractuales
     * @param result         resultado del cálculo de horas extras
     */
    private static void printReport(String employeeName,
                                    double monthlySalary,
                                    double hoursWorked,
                                    double contractHours,
                                    OvertimeResult result) {
        System.out.println();
        System.out.println("=".repeat(55));
        System.out.println("           DETALLE DE HORAS EXTRAS");
        System.out.println("=".repeat(55));
        System.out.printf("  Empleado              : %s%n",          employeeName);
        System.out.printf("  Salario mensual       : $ %,.2f%n",     monthlySalary);
        System.out.printf("  Horas contractuales   : %.2f hrs%n",    contractHours);
        System.out.printf("  Horas trabajadas      : %.2f hrs%n",    hoursWorked);
        System.out.println("-".repeat(55));
        System.out.printf("  Valor hora ordinaria  : $ %,.2f%n",     result.ordinaryHourlyRate());
        System.out.printf("  Valor hora extra (%sx): $ %,.2f%n",
                           OVERTIME_SURCHARGE_FACTOR,                 result.overtimeHourlyRate());
        System.out.printf("  Total horas extras    : %.2f hrs%n",    result.overtimeHours());
        System.out.println("=".repeat(55));
        System.out.printf("  TOTAL A PAGAR (H.E.)  : $ %,.2f%n",    result.totalOvertimePay());
        System.out.println("=".repeat(55));

        // Mensaje informativo si no hubo horas extras
        if (result.overtimeHours() == 0) {
            System.out.println("  [i] El empleado no registró horas extras este mes.");
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Clase de resultado (Record inmutable)
    // -----------------------------------------------------------------------

    /**
     * Contenedor inmutable con los resultados del cálculo de horas extras.
     *
     * @param ordinaryHourlyRate  valor de la hora ordinaria
     * @param overtimeHourlyRate  valor de la hora extra con recargo
     * @param overtimeHours       cantidad de horas extras trabajadas
     * @param totalOvertimePay    monto total a pagar por horas extras
     */
    private record OvertimeResult(
            double ordinaryHourlyRate,
            double overtimeHourlyRate,
            double overtimeHours,
            double totalOvertimePay) {
    }
}
