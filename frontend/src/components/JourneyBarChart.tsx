import React from "react";

import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
} from "chart.js";
import { Bar } from "react-chartjs-2";

// register chartjs component (needed for react)
ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
);

interface RouteStep {
    station: string;
    line: string;
    actualTime: number;
}

interface JourneyChartProps {
    path: RouteStep[];
}

export default function JourneyBarChart({ path }: JourneyChartProps) {
    // process graph data for chart
    // skip first station (index 0)
    const destinationStations = path.slice(1).map((step) => step.station);

    // calculate time taken for each individual step
    const stepDuration = path.slice(1).map((step, index) => {
        return step.actualTime - path[index].actualTime;
    });

    // Chart Data
    const data = {
        labels: destinationStations,
        datasets: [
            {
                label: "Minutes per step",
                data: stepDuration,
                backgroundColor: [
                    "rgba(255, 99, 132, 0.2)",
                    "rgba(255, 159, 64, 0.2)",
                    "rgba(255, 205, 86, 0.2)",
                    "rgba(75, 192, 192, 0.2)",
                    "rgba(54, 162, 235, 0.2)",
                    "rgba(153, 102, 255, 0.2)",
                    "rgba(201, 203, 207, 0.2)",
                ],
                borderColor: [
                    "rgb(255, 99, 132)",
                    "rgb(255, 159, 64)",
                    "rgb(255, 205, 86)",
                    "rgb(75, 192, 192)",
                    "rgb(54, 162, 235)",
                    "rgb(153, 102, 255)",
                    "rgb(201, 203, 207)",
                ],
                borderWidth: 2,
                borderRadius: 4,
            },
        ],
    };

    const options = {
        responsive: true,
        plugins: {
            legend: {
                position: "top" as const,
            },
            title: {
                display: true,
                text: "Journey Breakdown",
                font: { size: 18 },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: "minutes",
                },
            },
        },
    };

    return (
        <div className="card bg-base-100 shadow-xl mt-8 border-t-4 border-info">
            <div className="card-body">
                <Bar data={data} options={options} />
            </div>
        </div>
    );
}
