<template>
  <Bar :data="chartData" :options="mergedOptions" />
</template>

<script>
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

export default {
  name: 'HorizontalBarChart',
  components: { Bar },
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
        indexAxis: 'y',
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
          x: {
            beginAtZero: true,
            grid: { color: 'rgba(0, 0, 0, 0.04)' },
            ticks: {
              font: { size: 11 },
              color: '#999',
            },
          },
          y: {
            grid: { display: false },
            ticks: {
              font: { size: 12 },
              color: '#333',
            },
          },
        },
        ...this.options,
      }
    },
  },
}
</script>
