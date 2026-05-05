/* Chart.js helper functions used across dashboard and reports pages */

Chart.defaults.font.family = "'Inter', sans-serif";
Chart.defaults.font.size = 12;

/**
 * Pie / Doughnut chart — expense breakdown by category
 */
function initPieChart(canvasId, labels, data, colors) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors,
                borderWidth: 2,
                borderColor: '#fff',
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            cutout: '62%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 12,
                        usePointStyle: true,
                        pointStyleWidth: 10,
                        font: { size: 11 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            const total = ctx.dataset.data.reduce((a, b) => a + b, 0);
                            const pct = total > 0 ? ((ctx.raw / total) * 100).toFixed(1) : 0;
                            return ' ₹' + Number(ctx.raw).toLocaleString('en-IN', {minimumFractionDigits: 2}) + '  (' + pct + '%)';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Bar chart — income vs expense over months
 */
function initBarChart(canvasId, labels, incomeData, expenseData) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Income',
                    data: incomeData,
                    backgroundColor: 'rgba(16, 185, 129, 0.8)',
                    borderColor: '#10B981',
                    borderWidth: 1,
                    borderRadius: 5,
                    borderSkipped: false
                },
                {
                    label: 'Expense',
                    data: expenseData,
                    backgroundColor: 'rgba(239, 68, 68, 0.8)',
                    borderColor: '#EF4444',
                    borderWidth: 1,
                    borderRadius: 5,
                    borderSkipped: false
                }
            ]
        },
        options: {
            responsive: true,
            interaction: { mode: 'index', intersect: false },
            scales: {
                x: { grid: { display: false } },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,.05)' },
                    ticks: {
                        callback: function(val) {
                            return '₹' + Number(val).toLocaleString('en-IN');
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: { usePointStyle: true, pointStyleWidth: 10, padding: 16 }
                },
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            return ' ' + ctx.dataset.label + ': ₹' +
                                Number(ctx.raw).toLocaleString('en-IN', {minimumFractionDigits: 2});
                        }
                    }
                }
            }
        }
    });
}

/**
 * Line chart — daily spending trend
 */
function initLineChart(canvasId, labels, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Daily Spending',
                data: data,
                borderColor: '#4F46E5',
                backgroundColor: 'rgba(79, 70, 229, 0.08)',
                borderWidth: 2.5,
                fill: true,
                tension: 0.4,
                pointRadius: 3,
                pointHoverRadius: 5,
                pointBackgroundColor: '#4F46E5'
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: { grid: { display: false } },
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,.05)' },
                    ticks: {
                        callback: function(val) {
                            return '₹' + Number(val).toLocaleString('en-IN');
                        }
                    }
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            return ' ₹' +
                                Number(ctx.raw).toLocaleString('en-IN', {minimumFractionDigits: 2});
                        }
                    }
                }
            }
        }
    });
}

/**
 * Horizontal bar chart — category breakdown (monthly report)
 */
function initCategoryBarChart(canvasId, labels, data, colors) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Amount',
                data: data,
                backgroundColor: colors,
                borderRadius: 4
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            scales: {
                x: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,.05)' },
                    ticks: {
                        callback: function(val) {
                            return '₹' + Number(val).toLocaleString('en-IN');
                        }
                    }
                },
                y: { grid: { display: false } }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            return ' ₹' +
                                Number(ctx.raw).toLocaleString('en-IN', {minimumFractionDigits: 2});
                        }
                    }
                }
            }
        }
    });
}
