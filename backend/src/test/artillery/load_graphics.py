import json
import pandas as pd
import matplotlib.pyplot as plt

try:
    with open('report.json', 'r') as file:
        data = json.load(file)
except FileNotFoundError:
    print("Error: The file 'report.json'. could not be found")
    exit()

intervals = data.get('intermediate', [])
records = []

for i, interval in enumerate(intervals):
    min = (i * 10) / 60.0
    
    rates = interval.get('rates') or {}
    req_rate = rates.get('http.request_rate') or 0
    
    summaries = interval.get('summaries') or {}
    http_summary = summaries.get('http.response_time') or {}
    p50 = http_summary.get('p50', 0)
    p95 = http_summary.get('p95', 0)
    p99 = http_summary.get('p99', 0)
    
    counters = interval.get('counters') or {}
    errors_4xx = counters.get('http.codes.400', 0)
    successes_2xx = counters.get('http.codes.200', 0)
    successes_201 = counters.get('http.codes.201', 0)
    total_successes = successes_2xx + successes_201
    
    records.append({
        'Tiempo (min)': round(min, 2),
        'Peticiones/seg (RPS)': req_rate,
        'Mediana - p50 (ms)': p50,
        'Percentil 95 - p95 (ms)': p95,
        'Percentil 99 - p99 (ms)': p99,
        'Éxitos (2xx)': total_successes,
        'Errores Concurrentes (400)': errors_4xx
    })

df = pd.DataFrame(records)
df.to_excel('excel_report.xlsx', index=False)
print("✅ Generated Excel file: 'excel_report.xlsx'")


plt.style.use('seaborn-v0_8-darkgrid')
fig, (ax1, ax2, ax3) = plt.subplots(3, 1, figsize=(10, 12), sharex=True)

# Chart: Response Times
ax1.plot(df['Tiempo (min)'], df['Percentil 99 - p99 (ms)'], label='p99 (Picos Extremos)', color='#FF6B6B', linewidth=1.5, alpha=0.8)
ax1.plot(df['Tiempo (min)'], df['Percentil 95 - p95 (ms)'], label='p95 (Latencia Alta)', color='#FFD166', linewidth=2)
ax1.plot(df['Tiempo (min)'], df['Mediana - p50 (ms)'], label='Mediana (Rendimiento Normal)', color='#06D6A0', linewidth=2)
ax1.set_title('Tiempo de respuesta HTTP (Latencia)', fontsize=14, pad=10, fontweight='bold')
ax1.set_ylabel('Milisegundos (ms)', fontsize=10)
ax1.legend(loc='upper left')
ax1.set_ylim(bottom=0)

# Chart: Throughput
ax2.plot(df['Tiempo (min)'], df['Peticiones/seg (RPS)'], color='#118AB2', linewidth=2)
ax2.fill_between(df['Tiempo (min)'], df['Peticiones/seg (RPS)'], color='#118AB2', alpha=0.2)
ax2.set_title('Tasa de solicitudes HTTP (Carga del Servidor)', fontsize=14, pad=10, fontweight='bold')
ax2.set_ylabel('Peticiones / seg', fontsize=10)
ax2.set_ylim(bottom=0)

# Chart: Successes vs. Failures
ax3.bar(df['Tiempo (min)'], df['Éxitos (2xx)'], color='#06D6A0', label='Respuestas 200/201 (Éxito)', width=0.12, alpha=0.8)
ax3.bar(df['Tiempo (min)'], df['Errores Concurrentes (400)'], bottom=df['Éxitos (2xx)'], color='#EF476F', label='Errores 400 (Colisión de Reservas)', width=0.12, alpha=0.9)
ax3.set_title('Distribución de Códigos HTTP', fontsize=14, pad=10, fontweight='bold')
ax3.set_xlabel('Tiempo de la prueba (Minutos)', fontsize=11, fontweight='bold')
ax3.set_ylabel('Cantidad de Peticiones', fontsize=10)
ax3.legend(loc='upper left')

plt.tight_layout()
plt.savefig('artillery_graphics.png', dpi=300)
print("✅ Generated graphics: 'artillery_graphics.png'")