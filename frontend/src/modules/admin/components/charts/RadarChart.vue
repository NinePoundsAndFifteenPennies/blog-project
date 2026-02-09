<template>
  <Radar :data="chartData" :options="mergedOptions" />
</template>

<script>
import { Radar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend,
} from 'chart.js'

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend)

export default {
  name: 'RadarChart',
  components: { Radar },
  props: {
    chartData: {
      type: Object,
      required: true,
    },
    options: {
      type: Object,
      default: () => ({}),
    },
  },
  computed: {
    mergedOptions() {
      return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false,
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            padding: 12,
            titleFont: { size: 13 },
            bodyFont: { size: 12 },
            cornerRadius: 8,
          },
        },
        scales: {
          r: {
            angleLines: {
              color: 'rgba(0, 0, 0, 0.06)',
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.06)',
            },
            pointLabels: {
              font: { size: 12 },
              color: '#666',
            },
            ticks: {
              display: false,
            },
            suggestedMin: 0,
            suggestedMax: 100,
          },
        },
        ...this.options,
      }
    },
  },
}
</script>
