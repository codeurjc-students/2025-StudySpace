import json
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# load .json
try:
    with open('report.json', 'r', encoding='utf-8') as file:
        data = json.load(file)
except FileNotFoundError:
    print("❌ Error: The file 'report.json' could not be found.")
    exit()

intervals = data.get('intermediate', [])
records = []

#procesing 
for i, interval in enumerate(intervals):
    minuto = (i * 10) / 60.0 
    
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
    
    #VU
    vusers_created = counters.get('vusers.created', 0)
    vusers_active = counters.get('vusers.active', 0)
    vusers_failed = counters.get('vusers.failed', 0)
    
    records.append({
        'Tiempo (min)': round(minuto, 2),
        'Peticiones/seg (RPS)': req_rate,
        'Mediana - p50 (ms)': p50,
        'Percentil 95 - p95 (ms)': p95,
        'Percentil 99 - p99 (ms)': p99,
        'Éxitos (2xx/201)': total_successes,
        'Errores (400)': errors_4xx,
        'VUsers Creados': vusers_created,
        'VUsers Activos': vusers_active,
        'VUsers Fallidos': vusers_failed
    })

df_time = pd.DataFrame(records)
df_time.to_excel('reporte_artillery_series.xlsx', index=False)
print("✅ Excel Time Series generated: 'reporte_artillery_series.xlsx'")

# Endpoints
endpoint_records = []
agg_summaries = data.get('aggregate', {}).get('summaries', {})

for key, metrics in agg_summaries.items():
    if key.startswith('plugins.metrics-by-endpoint.response_time.'):
        #only end of the route
        endpoint = key.replace('plugins.metrics-by-endpoint.response_time.', '')
        endpoint_records.append({
            'Endpoint': endpoint,
            'min': metrics.get('min', 0),
            'mean': metrics.get('mean', 0),
            'p50': metrics.get('p50', 0),
            'p95': metrics.get('p95', 0),
            'p99': metrics.get('p99', 0),
            'max': metrics.get('max', 0)
        })

df_endpoints = pd.DataFrame(endpoint_records)
if not df_endpoints.empty:
    df_endpoints.to_excel('reporte_artillery_endpoints.xlsx', index=False)
    print("✅ Excel de Endpoints generado: 'reporte_artillery_endpoints.xlsx'")

#graphs
plt.style.use('seaborn-v0_8-darkgrid')

#DASHBOARD 1
fig, (ax1, ax2, ax3, ax4) = plt.subplots(4, 1, figsize=(12, 16), sharex=True)

#Panel 1: General Latencies
ax1.plot(df_time['Tiempo (min)'], df_time['Percentil 99 - p99 (ms)'], label='p99 (Picos Extremos)', color='#FF6B6B', linewidth=1.5, alpha=0.8)
ax1.plot(df_time['Tiempo (min)'], df_time['Percentil 95 - p95 (ms)'], label='p95 (Latencia Alta)', color='#FFD166', linewidth=2)
ax1.plot(df_time['Tiempo (min)'], df_time['Mediana - p50 (ms)'], label='Mediana (p50)', color='#06D6A0', linewidth=2)
ax1.set_title('Tiempos de Respuesta HTTP (Latencia General)', fontsize=14, fontweight='bold', pad=10)
ax1.set_ylabel('Milisegundos (ms)')
ax1.legend(loc='upper left')
ax1.set_ylim(bottom=0)

# Panel 2: Correlation between Load and Active Users (Dual Y-Axis)
color_rps = '#118AB2'
ax2.plot(df_time['Tiempo (min)'], df_time['Peticiones/seg (RPS)'], color=color_rps, linewidth=2.5, label='Request Rate (RPS)')
ax2.fill_between(df_time['Tiempo (min)'], df_time['Peticiones/seg (RPS)'], color=color_rps, alpha=0.15)
ax2.set_ylabel('RPS (Peticiones/seg)', color=color_rps, fontweight='bold')
ax2.tick_params(axis='y', labelcolor=color_rps)

ax2_twin = ax2.twinx()
color_vu = '#8338EC'
ax2_twin.plot(df_time['Tiempo (min)'], df_time['VUsers Activos'], color=color_vu, linewidth=2, linestyle='--', label='VUsers Activos')
ax2_twin.plot(df_time['Tiempo (min)'], df_time['VUsers Creados'], color='#9D4EDD', linewidth=1.5, alpha=0.6, label='VUsers Creados')
ax2_twin.set_ylabel('Usuarios Virtuales', color=color_vu, fontweight='bold')
ax2_twin.tick_params(axis='y', labelcolor=color_vu)

ax2.set_title('Rendimiento: Tasa de Peticiones HTTP vs Usuarios Virtuales Activos', fontsize=14, fontweight='bold', pad=10)
# We unified the legends of both axes
lines_1, labels_1 = ax2.get_legend_handles_labels()
lines_2, labels_2 = ax2_twin.get_legend_handles_labels()
ax2.legend(lines_1 + lines_2, labels_1 + labels_2, loc='upper left')
ax2.set_ylim(bottom=0)
ax2_twin.set_ylim(bottom=0)

# Panel 3: Degradation Correlation (p95 Latency vs Failed VUsers)
color_p95 = '#E09F3E'
ax3.plot(df_time['Tiempo (min)'], df_time['Percentil 95 - p95 (ms)'], color=color_p95, linewidth=2, label='Latencia p95 (ms)')
ax3.fill_between(df_time['Tiempo (min)'], df_time['Percentil 95 - p95 (ms)'], color=color_p95, alpha=0.2)
ax3.set_ylabel('Latencia p95 (ms)', color=color_p95, fontweight='bold')
ax3.tick_params(axis='y', labelcolor=color_p95)

ax3_twin = ax3.twinx()
color_failed = '#D90429'
ax3_twin.plot(df_time['Tiempo (min)'], df_time['VUsers Fallidos'], color=color_failed, linewidth=2.5, linestyle='-.', label='VUsers Fallidos')
ax3_twin.set_ylabel('Usuarios Fallidos', color=color_failed, fontweight='bold')
ax3_twin.tick_params(axis='y', labelcolor=color_failed)

ax3.set_title('Impacto: Degradación de Latencia (p95) vs Fallos de Usuarios', fontsize=14, fontweight='bold', pad=10)
lines_3, labels_3 = ax3.get_legend_handles_labels()
lines_4, labels_4 = ax3_twin.get_legend_handles_labels()
ax3.legend(lines_3 + lines_4, labels_3 + labels_4, loc='upper left')
ax3.set_ylim(bottom=0)
ax3_twin.set_ylim(bottom=0)

# Panel 4: HTTP Codes
ax4.bar(df_time['Tiempo (min)'], df_time['Éxitos (2xx/201)'], color='#06D6A0', label='Respuestas 200/201 (Éxito)', width=0.12, alpha=0.8)
ax4.bar(df_time['Tiempo (min)'], df_time['Errores (400)'], bottom=df_time['Éxitos (2xx/201)'], color='#EF476F', label='Errores 400 (Regla Negocio)', width=0.12, alpha=0.9)
ax4.set_title('Distribución de Códigos HTTP', fontsize=14, fontweight='bold', pad=10)
ax4.set_xlabel('Tiempo de la prueba (Minutos)', fontsize=12, fontweight='bold')
ax4.set_ylabel('Cantidad de Peticiones')
ax4.legend(loc='upper left')

plt.tight_layout()
plt.savefig('graficas_artillery_dashboard.png', dpi=300)
print("✅ Dashboard temporal generado: 'graficas_artillery_dashboard.png'")

#DASHBOARD 2: Breakdown by Endpoint (Grouped Bar Chart)
if not df_endpoints.empty:
    # Sort by p95 in descending order so that the slowest endpoints appear first
    df_endpoints = df_endpoints.sort_values(by='p95', ascending=False)
    
    df_endpoints.set_index('Endpoint', inplace=True)
    
    # Colors for min, mean, p50, p95, p99, max
    colores = ['#4CC9F0', '#4895EF', '#4361EE', '#F72585', '#E80505', '#7209B7']
    
    ax_bar = df_endpoints.plot(kind='bar', figsize=(12, 7), color=colores, width=0.8, edgecolor='white')
    
    plt.title('Rendimiento y Tiempos de Respuesta por Endpoint', fontsize=16, fontweight='bold', pad=15)
    plt.ylabel('Milisegundos (ms)', fontsize=12, fontweight='bold')
    plt.xlabel('Endpoint', fontsize=12, fontweight='bold')
    plt.xticks(rotation=15, ha='right', fontsize=10)
    plt.grid(axis='y', linestyle='--', alpha=0.7)
    
    #Label out of the graph
    plt.legend(title='Métricas de Latencia', bbox_to_anchor=(1.01, 1), loc='upper left')
    
    plt.tight_layout()
    plt.savefig('graficas_endpoints_barras.png', dpi=300)
    print("✅ Generated Endpoints Chart: 'graficas_endpoints_barras.png'")
else:
    print("⚠️ No endpoint metrics were found in the JSON (you need the metrics-by-endpoint plugin active in Artillery).")