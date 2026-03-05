using System;

/// <summary>
/// Programa de consola para calcular el valor y total de horas extras de un empleado.
/// Solicita al usuario los datos necesarios (nombre, salario mensual, horas trabajadas
/// y horas ordinarias contractuales) y calcula el valor de la hora extra y el monto total
/// a pagar por dichas horas.
/// </summary>
/// <author>Elias Santander</author>
/// <version>1.0</version>
internal class OvertimeCalculator
{
    // -----------------------------------------------------------------------
    // Constantes de negocio
    // -----------------------------------------------------------------------

    /// <summary>Número de días laborables en un mes estándar.</summary>
    private const int WorkingDaysPerMonth = 30;

    /// <summary>Número de horas laborables en un día estándar.</summary>
    private const int WorkingHoursPerDay = 8;

    /// <summary>Factor de recargo aplicado sobre el valor de la hora ordinaria para calcular la hora extra.</summary>
    private const double OvertimeSurchargeFactor = 1.5;

    // -----------------------------------------------------------------------
    // Punto de entrada
    // -----------------------------------------------------------------------

    /// <summary>
    /// Método principal que ejecuta el flujo completo del programa.
    /// </summary>
    /// <param name="args">Argumentos de línea de comandos (no se utilizan).</param>
    private static void Main(string[] args)
    {
        try
        {
            PrintHeader();

            // --- Recolección de datos ---
            string employeeName  = ReadEmployeeName();
            double monthlySalary = ReadPositiveDouble("Ingrese el salario mensual bruto ($): ");
            double hoursWorked   = ReadPositiveDouble("Ingrese el total de horas trabajadas este mes: ");
            double contractHours = ReadContractHours();

            // --- Cálculo ---
            OvertimeResult result = CalculateOvertime(monthlySalary, hoursWorked, contractHours);

            // --- Reporte ---
            PrintReport(employeeName, monthlySalary, hoursWorked, contractHours, result);
        }
        catch (Exception ex)
        {
            // Captura cualquier excepción inesperada y muestra mensaje amigable
            Console.Error.WriteLine($"\n[ERROR] Ocurrió un error inesperado: {ex.Message}");
        }
    }

    // -----------------------------------------------------------------------
    // Métodos de lectura / validación
    // -----------------------------------------------------------------------

    /// <summary>
    /// Imprime el encabezado decorativo del programa.
    /// </summary>
    private static void PrintHeader()
    {
        Console.WriteLine(new string('=', 55));
        Console.WriteLine("       CALCULADORA DE HORAS EXTRAS");
        Console.WriteLine(new string('=', 55));
    }

    /// <summary>
    /// Lee y valida el nombre del empleado (no puede estar vacío).
    /// </summary>
    /// <returns>Nombre del empleado ingresado por el usuario.</returns>
    private static string ReadEmployeeName()
    {
        while (true)
        {
            Console.Write("Ingrese el nombre del empleado: ");
            string? input = Console.ReadLine()?.Trim();

            // Validación: el nombre no puede estar vacío ni nulo
            if (!string.IsNullOrEmpty(input))
                return input;

            Console.WriteLine("  [!] El nombre no puede estar vacío. Intente nuevamente.");
        }
    }

    /// <summary>
    /// Lee un número decimal positivo desde la consola, reintentando si el valor
    /// ingresado no es válido o es menor o igual a cero.
    /// </summary>
    /// <param name="prompt">Mensaje que se muestra al usuario antes de leer el valor.</param>
    /// <returns>Valor decimal positivo ingresado por el usuario.</returns>
    private static double ReadPositiveDouble(string prompt)
    {
        while (true)
        {
            Console.Write(prompt);
            string? input = Console.ReadLine();

            // Intentamos parsear el valor; si falla o es negativo, volvemos a pedir
            if (double.TryParse(input, out double value) && value > 0)
                return value;

            Console.WriteLine("  [!] Entrada inválida. Ingrese un número mayor que cero.");
        }
    }

    /// <summary>
    /// Lee las horas contractuales ordinarias del mes, ofreciendo un valor
    /// por defecto calculado a partir de <see cref="WorkingDaysPerMonth"/> y
    /// <see cref="WorkingHoursPerDay"/>.
    /// </summary>
    /// <returns>Total de horas ordinarias pactadas en el contrato.</returns>
    private static double ReadContractHours()
    {
        // Calculamos el valor por defecto (ej: 30 días × 8 horas = 240)
        double defaultHours = WorkingDaysPerMonth * WorkingHoursPerDay;
        Console.Write($"Ingrese las horas ordinarias contractuales del mes [{defaultHours:F0}]: ");

        string? input = Console.ReadLine()?.Trim();

        // Si el usuario presiona Enter sin ingresar nada, usamos el valor por defecto
        if (string.IsNullOrEmpty(input))
        {
            Console.WriteLine($"  [i] Se usará el valor por defecto: {defaultHours:F0} horas.");
            return defaultHours;
        }

        // Si ingresó un valor, lo validamos
        if (double.TryParse(input, out double value) && value > 0)
            return value;

        // Entrada inválida: fallback al valor por defecto
        Console.WriteLine("  [!] Entrada inválida. Se usará el valor por defecto.");
        return defaultHours;
    }

    // -----------------------------------------------------------------------
    // Lógica de negocio
    // -----------------------------------------------------------------------

    /// <summary>
    /// Calcula el valor de la hora ordinaria, el valor de la hora extra y
    /// el monto total a pagar por las horas extras trabajadas.
    /// <para>Fórmulas utilizadas:</para>
    /// <list type="bullet">
    ///   <item>Hora ordinaria  = salario mensual / horas contractuales</item>
    ///   <item>Hora extra      = hora ordinaria × <see cref="OvertimeSurchargeFactor"/></item>
    ///   <item>Horas extra     = horas trabajadas − horas contractuales (mínimo 0)</item>
    ///   <item>Total horas extra = horas extra × valor hora extra</item>
    /// </list>
    /// </summary>
    /// <param name="monthlySalary">Salario mensual bruto del empleado.</param>
    /// <param name="hoursWorked">Total de horas efectivamente trabajadas en el mes.</param>
    /// <param name="contractHours">Horas ordinarias pactadas en el contrato para el mes.</param>
    /// <returns>Instancia de <see cref="OvertimeResult"/> con todos los valores calculados.</returns>
    private static OvertimeResult CalculateOvertime(double monthlySalary,
                                                    double hoursWorked,
                                                    double contractHours)
    {
        // Valor de la hora ordinaria
        double ordinaryHourlyRate = monthlySalary / contractHours;

        // Valor de la hora extra con recargo
        double overtimeHourlyRate = ordinaryHourlyRate * OvertimeSurchargeFactor;

        // Horas extras (no puede ser negativo si el empleado no superó el límite)
        double overtimeHours = Math.Max(0, hoursWorked - contractHours);

        // Monto total a pagar por horas extras
        double totalOvertimePay = overtimeHours * overtimeHourlyRate;

        return new OvertimeResult(ordinaryHourlyRate, overtimeHourlyRate, overtimeHours, totalOvertimePay);
    }

    // -----------------------------------------------------------------------
    // Reporte
    // -----------------------------------------------------------------------

    /// <summary>
    /// Imprime el reporte detallado de horas extras en consola.
    /// </summary>
    /// <param name="employeeName">Nombre del empleado.</param>
    /// <param name="monthlySalary">Salario mensual bruto.</param>
    /// <param name="hoursWorked">Horas trabajadas en el mes.</param>
    /// <param name="contractHours">Horas ordinarias contractuales.</param>
    /// <param name="result">Resultado del cálculo de horas extras.</param>
    private static void PrintReport(string employeeName,
                                    double monthlySalary,
                                    double hoursWorked,
                                    double contractHours,
                                    OvertimeResult result)
    {
        Console.WriteLine();
        Console.WriteLine(new string('=', 55));
        Console.WriteLine("           DETALLE DE HORAS EXTRAS");
        Console.WriteLine(new string('=', 55));
        Console.WriteLine($"  Empleado              : {employeeName}");
        Console.WriteLine($"  Salario mensual       : $ {monthlySalary:N2}");
        Console.WriteLine($"  Horas contractuales   : {contractHours:F2} hrs");
        Console.WriteLine($"  Horas trabajadas      : {hoursWorked:F2} hrs");
        Console.WriteLine(new string('-', 55));
        Console.WriteLine($"  Valor hora ordinaria  : $ {result.OrdinaryHourlyRate:N2}");
        Console.WriteLine($"  Valor hora extra ({OvertimeSurchargeFactor}x): $ {result.OvertimeHourlyRate:N2}");
        Console.WriteLine($"  Total horas extras    : {result.OvertimeHours:F2} hrs");
        Console.WriteLine(new string('=', 55));
        Console.WriteLine($"  TOTAL A PAGAR (H.E.)  : $ {result.TotalOvertimePay:N2}");
        Console.WriteLine(new string('=', 55));

        // Mensaje informativo si no hubo horas extras
        if (result.OvertimeHours == 0)
            Console.WriteLine("  [i] El empleado no registró horas extras este mes.");

        Console.WriteLine();
    }

    // -----------------------------------------------------------------------
    // Clase de resultado (Record inmutable)
    // -----------------------------------------------------------------------

    /// <summary>
    /// Contenedor inmutable con los resultados del cálculo de horas extras.
    /// </summary>
    /// <param name="OrdinaryHourlyRate">Valor de la hora ordinaria.</param>
    /// <param name="OvertimeHourlyRate">Valor de la hora extra con recargo.</param>
    /// <param name="OvertimeHours">Cantidad de horas extras trabajadas.</param>
    /// <param name="TotalOvertimePay">Monto total a pagar por horas extras.</param>
    private record OvertimeResult(
        double OrdinaryHourlyRate,
        double OvertimeHourlyRate,
        double OvertimeHours,
        double TotalOvertimePay);
}
