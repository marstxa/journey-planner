import {
    CategoryScale,
    Chart as ChartJS,
    Legend,
    LinearScale,
    LineElement,
    PointElement,
    Title,
    Tooltip,
    type TooltipItem,
} from "chart.js";
import { Line } from "react-chartjs-2";

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
);

interface RouteStep {
    station: string;
    line: string;
    actualTime: number;
}

interface Props {
    fastestPath: RouteStep[];
    fewestPath: RouteStep[];
}

const FASTEST_LABEL = "Fastest Time Route";
const FEWEST_LABEL = "Fewest Changes Route";

export default function JourneyLineChart({ fastestPath, fewestPath }: Props) {
    // get the route with the most stops
    const maxStops = Math.max(fastestPath.length, fewestPath.length);
    const labels = Array.from({ length: maxStops }, (_, i) =>
        i === 0 ? "Start" : `Stop ${i}`,
    );

    const fastestTimes = fastestPath.map((step) => step.actualTime);
    const fewestTimes = fewestPath.map((step) => step.actualTime);

    // build both lines
    const data = {
        labels: labels,
        datasets: [
            {
                label: FASTEST_LABEL,
                data: fastestTimes,
                borderColor: "rgba(45, 196, 155, 1)",
                backgroundColor: "rgba(45, 196, 155, 1)",
                tension: 0.3,
                borderWidth: 3,
                pointRadius: 5,
            },
            {
                label: FEWEST_LABEL,
                data: fewestTimes,
                borderColor: "rgba(238, 175, 197, 1)",
                backgroundColor: "rgba(238, 175, 197, 1)",
                tension: 0.3,
                borderWidth: 3,
                pointRadius: 5,
                borderDash: [5, 5], // makes the Fewest Changes line dotted to tell them apart
            },
        ],
    };

    const options = {
        responsive: true,
        plugins: {
            legend: { position: "top" as const },
            title: {
                display: true,
                text: "Route: Time vs. Changes",
                font: { size: 18 },
            },
            tooltip: {
                callbacks: {
                    // Previously used context.datasetIndex === 0 to pick which path
                    // a point belonged to, which silently breaks if the datasets
                    // array above is ever reordered. Matching on the dataset's own
                    // label ties it directly to the data it's rendering instead.
                    label: function (context: TooltipItem<"line">) {
                        const isFastest = context.dataset.label === FASTEST_LABEL;
                        const path = isFastest ? fastestPath : fewestPath;
                        const stepIndex = context.dataIndex;

                        if (stepIndex < path.length) {
                            return `${path[stepIndex].station}: ${context.raw} mins`;
                        }
                        return `${context.raw} mins`;
                    },
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                title: { display: true, text: "minutes" },
            },
        },
    };

    return (
        <div className="card bg-base-100 shadow-xl mb-8 border-t-4 border-accent">
            <div className="card-body">
                <Line data={data} options={options} />
            </div>
        </div>
    );
}