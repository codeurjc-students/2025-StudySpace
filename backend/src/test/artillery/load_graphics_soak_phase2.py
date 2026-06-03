import json
import pandas as pd
import matplotlib.pyplot as plt

def generate_graphs(json_filename):
    try:
        with open(json_filename, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"❌ Error reading the file {json_filename}: {e}")
        return

    intervals = data.get('intermediate', [])
    records_time = []

    for i, interval in enumerate(intervals):
        minuto = (i * 10) / 60.0  # Transform 10 s to minutes
        
        rates = interval.get('rates', {})
        counters = interval.get('counters', {})
        summaries = interval.get('summaries', interval.get('histograms', {}))
        resp_time = summaries.get('http.response_time', {})
        
        #http
        req_rate = rates.get('http.request_rate', 0)
        
        #p95
        p95 = resp_time.get('p95', 0)
        
        #Add the logic errors
        errores_intervalo = (
            counters.get('errors.ETIMEDOUT', 0) +
            counters.get('errors.ECONNRESET', 0) +
            counters.get('http.codes.500', 0) +
            counters.get('http.codes.400', 0)
        )
        
        records_time.append({
            'Tiempo': minuto,
            'Request Rate': req_rate,
            'Latencia p95': p95,
            'Errores': errores_intervalo
        })

    df = pd.DataFrame(records_time)

    # Smoothing
    window_size = max(1, len(df) // 50)  
    
    df['ReqRate_Smooth'] = df['Request Rate'].rolling(window=window_size, min_periods=1).mean()
    df['Latencia_Smooth'] = df['Latencia p95'].rolling(window=window_size, min_periods=1).mean()
    
    df['Errores_Agrupados'] = df['Errores'].rolling(window=window_size, min_periods=1).sum()

    #axis y left
    plt.style.use('seaborn-v0_8-darkgrid')
    fig, ax1 = plt.subplots(figsize=(12, 6))

    color_req = '#00B4D8'  
    color_err = '#E63946'   
    
    l1 = ax1.plot(df['Tiempo'], df['ReqRate_Smooth'], color=color_req, linewidth=2.5, label='Request Rate (req/s)')
    ax1.fill_between(df['Tiempo'], df['ReqRate_Smooth'], color=color_req, alpha=0.1)
    
    l2 = ax1.plot(df['Tiempo'], df['Errores_Agrupados'], color=color_err, linewidth=2, linestyle='--', label='Errores (Timeouts)')
    
    ax1.set_xlabel('Tiempo (Minutos)', fontweight='bold')
    ax1.set_ylabel('Peticiones por Segundo', fontweight='bold')
    ax1.set_ylim(bottom=0)

    # y rigth
    ax2 = ax1.twinx()
    color_lat = '#9D4EDD'   
    
    l3 = ax2.plot(df['Tiempo'], df['Latencia_Smooth'], color=color_lat, linewidth=2.5, label='Latencia p95 (ms)')
    ax2.set_ylabel('Milisegundos (ms)', color=color_lat, fontweight='bold')
    ax2.tick_params(axis='y', labelcolor=color_lat)
    ax2.set_ylim(bottom=0)
    ax2.grid(False)

    #labels
    lines = l1 + l3 + l2
    labels = [l.get_label() for l in lines]
    ax1.legend(lines, labels, loc='upper center', bbox_to_anchor=(0.5, -0.15), ncol=3, frameon=False, fontsize=11)

    plt.title(f'Rendimiento del Servidor a lo largo del tiempo ()', fontweight='bold', fontsize=14, pad=15)
    plt.tight_layout()
    
    file_timeline = f'timeline_graph.png'
    plt.savefig(file_timeline, dpi=300, bbox_inches='tight')
    plt.close()
    print(f"✅ Generated Timeline: {file_timeline}")

    # Endpoints
    agg_data = data.get('aggregate', {})
    agg_metrics = agg_data.get('summaries', agg_data.get('histograms', {}))
    
    metrics_keys = ['min', 'mean', 'p50', 'p75', 'p90', 'p95', 'p99', 'max']
    endpoint_data = {}

    for key, metrics in agg_metrics.items():
        if key.startswith('plugins.metrics-by-endpoint.response_time.'):
            endpoint_name = key.replace('plugins.metrics-by-endpoint.response_time.', '')
            if endpoint_name.strip():
                endpoint_data[endpoint_name] = [metrics.get(m, 0) for m in metrics_keys]

    if endpoint_data:
        fig2, ax_dist = plt.subplots(figsize=(10, 6))
        colores = ['#00B4D8', '#F4A261', '#E63946', '#9D4EDD', '#2A9D8F']
        
        for i, (endpoint, values) in enumerate(endpoint_data.items()):
            ax_dist.plot(metrics_keys, values, marker='o', linewidth=2.5, markersize=6, 
                         color=colores[i % len(colores)], label=endpoint)

        ax_dist.set_title(f'Distribución de Tiempos por Endpoint', fontsize=14, fontweight='bold', pad=15)
        ax_dist.set_ylabel('Milisegundos (ms)', fontweight='bold')
        ax_dist.set_ylim(bottom=0)
        ax_dist.grid(axis='x', linestyle='--', alpha=0.5)

        ax_dist.legend(title='Endpoints', loc='center left', bbox_to_anchor=(1.02, 0.5), fontsize=10)
        
        plt.tight_layout()
        file_distribucion = f'generic_graph.png'
        plt.savefig(file_distribucion, dpi=300, bbox_inches='tight')
        plt.close()
        print(f"✅ Endpoints generated: {file_distribucion}")







generate_graphs('report.json')
