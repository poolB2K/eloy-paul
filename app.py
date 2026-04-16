import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from fpdf import FPDF
from datetime import datetime

st.set_page_config(page_title="ErgoSilla PRO", layout="wide")
st.title("ErgoSilla PRO - Sistema Antropometrico + Diseno de Silla")
st.markdown("**Version Streamlit** - Llena los datos y todo se actualiza en tiempo real")

with st.sidebar:
    st.header("Configuracion")
    n_alumnos = st.number_input("Numero de alumnos", value=7, min_value=1, max_value=30)
    regenerar = st.button("Regenerar datos de ejemplo")
    st.info("49 medidas por alumno segun catalogo oficial")


def generar_dataframe(n: int) -> pd.DataFrame:
    rng = np.random.default_rng(42)
    return pd.DataFrame({
        "Alumno": [f"Alumno_{i}" for i in range(1, n + 1)],
        "Sexo": ["Mujer" if i % 2 == 1 else "Hombre" for i in range(1, n + 1)],
        "Altura del cuerpo": rng.normal(165, 8, n).round(1),
        "Peso del cuerpo": rng.normal(65, 12, n).round(1),
        "Anchura de cadera, sentado": rng.normal(35, 3, n).round(1),
        "Largura del muslo, sentado": rng.normal(45, 3, n).round(1),
        "Altura a la rodilla, sentado": rng.normal(50, 4, n).round(1),
    })


if "df" not in st.session_state or regenerar or len(st.session_state.df) != n_alumnos:
    st.session_state.df = generar_dataframe(n_alumnos)

tab1, tab2, tab3 = st.tabs(["Base de Datos", "Percentiles & Diseno", "Reporte PDF"])

with tab1:
    st.subheader("Captura de las 49 medidas")
    edited_df = st.data_editor(
        st.session_state.df,
        num_rows="dynamic",
        use_container_width=True,
        key="editor",
    )

with tab2:
    st.subheader("Percentiles calculados")
    numeric_cols = edited_df.select_dtypes(include=np.number).columns
    perc = {}
    for col in numeric_cols:
        serie = edited_df[col].dropna()
        mu = serie.mean() if len(serie) else 0.0
        sigma = serie.std(ddof=1) if len(serie) > 1 else 0.0
        perc[col] = {
            "P5":  round(mu - 1.645 * sigma, 2),
            "P50": round(mu, 2),
            "P95": round(mu + 1.645 * sigma, 2),
        }
    perc_df = pd.DataFrame(perc).T
    st.dataframe(perc_df, use_container_width=True)

    st.subheader("Diseno automatico de la silla")
    sw = sd = sh_p5 = sh_p95 = 0.0

    if "Anchura de cadera, sentado" in perc_df.index:
        sw = float(perc_df.loc["Anchura de cadera, sentado", "P95"]) + 2
    if "Largura del muslo, sentado" in perc_df.index:
        sd = max(float(perc_df.loc["Largura del muslo, sentado", "P95"]) - 3, 0.0)
    if "Altura a la rodilla, sentado" in perc_df.index:
        sh_p5 = float(perc_df.loc["Altura a la rodilla, sentado", "P5"])
        sh_p95 = float(perc_df.loc["Altura a la rodilla, sentado", "P95"])

    c1, c2, c3 = st.columns(3)
    c1.metric("Ancho asiento", f"{sw:.1f} cm", "P95 + 2 cm")
    c2.metric("Profundidad asiento", f"{sd:.1f} cm", "P95 - 3 cm")
    c3.metric("Altura asiento", f"{sh_p5:.1f} - {sh_p95:.1f} cm", "Rango P5-P95")

    fig, ax = plt.subplots(figsize=(8, 5))
    sh = sh_p5 if sh_p5 > 0 else 45
    prof = sd if sd > 0 else 40
    # asiento (poligono cerrado)
    ax.fill([0, prof, prof, 0], [sh, sh, sh + 3, sh + 3], color="saddlebrown", alpha=0.8)
    # respaldo
    ax.fill([0, 3, 3, 0], [sh, sh, sh + 45, sh + 45], color="saddlebrown", alpha=0.8)
    # patas
    ax.plot([1, 1], [0, sh], "k-", linewidth=4)
    ax.plot([prof - 1, prof - 1], [0, sh], "k-", linewidth=4)
    ax.set_xlim(-5, prof + 10)
    ax.set_ylim(0, sh + 60)
    ax.set_title("Perfil lateral - silla ergonomica (parametros reales)")
    ax.set_xlabel("Profundidad (cm)")
    ax.set_ylabel("Altura (cm)")
    ax.grid(True, alpha=0.3)
    st.pyplot(fig)

with tab3:
    st.subheader("Generar reporte")
    if st.button("Generar PDF Profesional"):
        pdf = FPDF()
        pdf.add_page()
        pdf.set_font("Arial", "B", 16)
        pdf.cell(0, 10, f"Reporte ErgoSilla PRO - Equipo de {n_alumnos} alumnos",
                 ln=1, align="C")
        pdf.set_font("Arial", "", 10)
        pdf.cell(0, 8, f"Fecha: {datetime.now().strftime('%d/%m/%Y %H:%M')}", ln=1)
        pdf.ln(4)

        pdf.set_font("Arial", "B", 12)
        pdf.cell(0, 8, "Percentiles antropometricos (cm):", ln=1)
        pdf.set_font("Arial", "", 10)
        for idx, row in perc_df.iterrows():
            pdf.cell(0, 6,
                     f"  - {idx}: P5={row['P5']} | P50={row['P50']} | P95={row['P95']}",
                     ln=1)

        pdf.ln(4)
        pdf.set_font("Arial", "B", 12)
        pdf.cell(0, 8, "Diseno final de la silla:", ln=1)
        pdf.set_font("Arial", "", 10)
        pdf.cell(0, 6, f"  - Ancho del asiento:       {sw:.1f} cm", ln=1)
        pdf.cell(0, 6, f"  - Profundidad del asiento: {sd:.1f} cm", ln=1)
        pdf.cell(0, 6, f"  - Altura del asiento:      {sh_p5:.1f} - {sh_p95:.1f} cm", ln=1)

        pdf_bytes = bytes(pdf.output())
        st.download_button(
            "Descargar PDF ahora",
            data=pdf_bytes,
            file_name="Reporte_ErgoSilla_PRO.pdf",
            mime="application/pdf",
        )

st.success("Sistema listo. Llena los datos en la pestaña Base de Datos y todo se actualiza solo.")
