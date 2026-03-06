"""
overtime_calculator.py
======================
Programa de consola para calcular el valor y total de horas extras de un empleado.

Solicita al usuario los datos necesarios (nombre, salario mensual, horas trabajadas
y horas ordinarias contractuales) y calcula el valor de la hora extra y el monto
total a pagar por dichas horas.

Author : Elias Santander
Version: 1.0
"""

from dataclasses import dataclass


# ---------------------------------------------------------------------------
# Constantes de negocio
# ---------------------------------------------------------------------------

WORKING_DAYS_PER_MONTH: int = 30
"""Número de días laborables en un mes estándar."""

WORKING_HOURS_PER_DAY: int = 8
"""Número de horas laborables en un día estándar."""

OVERTIME_SURCHARGE_FACTOR: float = 1.5
"""Factor de recargo aplicado sobre el valor de la hora ordinaria para calcular la hora extra."""


# ---------------------------------------------------------------------------
# Clase de resultado (dataclass inmutable)
# ---------------------------------------------------------------------------

@dataclass(frozen=True)
class OvertimeResult:
    """
    Contenedor inmutable con los resultados del cálculo de horas extras.

    Attributes:
        ordinary_hourly_rate (float): Valor de la hora ordinaria.
        overtime_hourly_rate (float): Valor de la hora extra con recargo.
        overtime_hours       (float): Cantidad de horas extras trabajadas.
        total_overtime_pay   (float): Monto total a pagar por horas extras.
    """

    ordinary_hourly_rate: float
    overtime_hourly_rate: float
    overtime_hours: float
    total_overtime_pay: float


# ---------------------------------------------------------------------------
# Funciones de lectura / validación
# ---------------------------------------------------------------------------

def print_header() -> None:
    """Imprime el encabezado decorativo del programa."""
    print("=" * 55)
    print("       CALCULADORA DE HORAS EXTRAS")
    print("=" * 55)


def read_employee_name() -> str:
    """
    Lee y valida el nombre del empleado (no puede estar vacío).

    Returns:
        str: Nombre del empleado ingresado por el usuario.
    """
    while True:
        name = input("Ingrese el nombre del empleado: ").strip()

        # Validación: el nombre no puede estar vacío
        if name:
            return name

        print("  [!] El nombre no puede estar vacío. Intente nuevamente.")


def read_positive_float(prompt: str) -> float:
    """
    Lee un número decimal positivo desde la consola, reintentando si el valor
    ingresado no es válido o es menor o igual a cero.

    Args:
        prompt (str): Mensaje que se muestra al usuario antes de leer el valor.

    Returns:
        float: Valor decimal positivo ingresado por el usuario.
    """
    while True:
        try:
            value = float(input(prompt))

            # Validación: el valor debe ser mayor que cero
            if value > 0:
                return value

            print("  [!] El valor debe ser mayor que cero. Intente nuevamente.")

        except ValueError:
            # El usuario ingresó algo que no es un número
            print("  [!] Entrada inválida. Por favor ingrese un número válido.")


def read_contract_hours() -> float:
    """
    Lee las horas contractuales ordinarias del mes, ofreciendo un valor
    por defecto calculado a partir de ``WORKING_DAYS_PER_MONTH`` y
    ``WORKING_HOURS_PER_DAY``.

    Returns:
        float: Total de horas ordinarias pactadas en el contrato.
    """
    # Calculamos el valor por defecto (ej: 30 días × 8 horas = 240)
    default_hours: float = float(WORKING_DAYS_PER_MONTH * WORKING_HOURS_PER_DAY)

    raw = input(f"Ingrese las horas ordinarias contractuales del mes [{default_hours:.0f}]: ").strip()

    # Si el usuario presiona Enter sin ingresar nada, usamos el valor por defecto
    if not raw:
        print(f"  [i] Se usará el valor por defecto: {default_hours:.0f} horas.")
        return default_hours

    # Si ingresó un valor, lo validamos
    try:
        value = float(raw)
        if value > 0:
            return value

        # Valor no positivo: fallback al valor por defecto
        print("  [!] Valor inválido. Se usará el valor por defecto.")

    except ValueError:
        # El usuario ingresó texto no numérico; usamos el valor por defecto
        print("  [!] Entrada inválida. Se usará el valor por defecto.")

    return default_hours


# ---------------------------------------------------------------------------
# Lógica de negocio
# ---------------------------------------------------------------------------

def calculate_overtime(monthly_salary: float,
                        hours_worked: float,
                        contract_hours: float) -> OvertimeResult:
    """
    Calcula el valor de la hora ordinaria, el valor de la hora extra y
    el monto total a pagar por las horas extras trabajadas.

    Fórmulas utilizadas:

    - Hora ordinaria   = salario mensual / horas contractuales
    - Hora extra       = hora ordinaria × OVERTIME_SURCHARGE_FACTOR
    - Horas extras     = max(0, horas trabajadas − horas contractuales)
    - Total horas extra = horas extras × valor hora extra

    Args:
        monthly_salary (float): Salario mensual bruto del empleado.
        hours_worked   (float): Total de horas efectivamente trabajadas en el mes.
        contract_hours (float): Horas ordinarias pactadas en el contrato para el mes.

    Returns:
        OvertimeResult: Objeto con todos los valores calculados.
    """
    # Valor de la hora ordinaria
    ordinary_hourly_rate: float = monthly_salary / contract_hours

    # Valor de la hora extra con recargo
    overtime_hourly_rate: float = ordinary_hourly_rate * OVERTIME_SURCHARGE_FACTOR

    # Horas extras (no puede ser negativo si el empleado no superó el límite)
    overtime_hours: float = max(0.0, hours_worked - contract_hours)

    # Monto total a pagar por horas extras
    total_overtime_pay: float = overtime_hours * overtime_hourly_rate

    return OvertimeResult(
        ordinary_hourly_rate=ordinary_hourly_rate,
        overtime_hourly_rate=overtime_hourly_rate,
        overtime_hours=overtime_hours,
        total_overtime_pay=total_overtime_pay,
    )


# ---------------------------------------------------------------------------
# Reporte
# ---------------------------------------------------------------------------

def print_report(employee_name: str,
                 monthly_salary: float,
                 hours_worked: float,
                 contract_hours: float,
                 result: OvertimeResult) -> None:
    """
    Imprime el reporte detallado de horas extras en consola.

    Args:
        employee_name  (str):           Nombre del empleado.
        monthly_salary (float):         Salario mensual bruto.
        hours_worked   (float):         Horas trabajadas en el mes.
        contract_hours (float):         Horas ordinarias contractuales.
        result         (OvertimeResult): Resultado del cálculo de horas extras.
    """
    print()
    print("=" * 55)
    print("           DETALLE DE HORAS EXTRAS")
    print("=" * 55)
    print(f"  Empleado              : {employee_name}")
    print(f"  Salario mensual       : $ {monthly_salary:>12,.2f}")
    print(f"  Horas contractuales   : {contract_hours:.2f} hrs")
    print(f"  Horas trabajadas      : {hours_worked:.2f} hrs")
    print("-" * 55)
    print(f"  Valor hora ordinaria  : $ {result.ordinary_hourly_rate:>12,.2f}")
    print(f"  Valor hora extra ({OVERTIME_SURCHARGE_FACTOR}x): $ {result.overtime_hourly_rate:>12,.2f}")
    print(f"  Total horas extras    : {result.overtime_hours:.2f} hrs")
    print("=" * 55)
    print(f"  TOTAL A PAGAR (H.E.)  : $ {result.total_overtime_pay:>12,.2f}")
    print("=" * 55)

    # Mensaje informativo si no hubo horas extras
    if result.overtime_hours == 0:
        print("  [i] El empleado no registró horas extras este mes.")

    print()


# ---------------------------------------------------------------------------
# Punto de entrada
# ---------------------------------------------------------------------------

def main() -> None:
    """
    Función principal que ejecuta el flujo completo del programa.

    Orquesta la recolección de datos, el cálculo y la impresión del reporte.
    Captura cualquier excepción inesperada mostrando un mensaje amigable.
    """
    try:
        print_header()

        # --- Recolección de datos ---
        employee_name  = read_employee_name()
        monthly_salary = read_positive_float("Ingrese el salario mensual bruto ($): ")
        hours_worked   = read_positive_float("Ingrese el total de horas trabajadas este mes: ")
        contract_hours = read_contract_hours()

        # --- Cálculo ---
        result = calculate_overtime(monthly_salary, hours_worked, contract_hours)

        # --- Reporte ---
        print_report(employee_name, monthly_salary, hours_worked, contract_hours, result)

    except KeyboardInterrupt:
        # El usuario presionó Ctrl+C; salida limpia sin traceback
        print("\n\n  [i] Programa interrumpido por el usuario.")

    except Exception as ex:
        # Captura cualquier excepción inesperada y muestra mensaje amigable
        print(f"\n[ERROR] Ocurrió un error inesperado: {ex}")


if __name__ == "__main__":
    main()
