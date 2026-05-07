import { useEffect, useState } from "react";
import axios from "axios";
import JourneyBarChart from "./JourneyBarChart";

// toast library
import { ToastContainer, toast } from "react-toastify";
import JourneyLineChart from "./JourneyLineChart";
interface RouteStep {
    station: string;
    line: string;
    actualTime: number;
}

interface RouteData {
    totalTime: number;
    totalChanges: number;
    path: RouteStep[];
}

interface ApiResponse {
    error: string;
}

// line colors for ui
const getStepColorClass = (line: string) => {
    if (!line) return "step-primary";

    switch (line.toLowerCase()) {
        case "green":
            return "step-success";
        case "yellow":
            return "step-warning";
        case "red":
            return "step-error";
        case "lightblue":
            return "step-info";
        case "darkblue":
            return "step-primary";
        case "pink":
            return "step-secondary";
        case "purple":
            return "step-neutral";
        case "walking":
        case "start":
            return "";
        default:
            return "";
    }
};

export default function JourneyPlanner() {
    // form state
    const [start, setStart] = useState<string>("");
    const [end, setEnd] = useState<string>("");
    const [optimisedRoute, setOptimisedRoute] = useState<boolean>(true);

    // dual fetch both route options
    const [fastestPath, setFastestPath] = useState<RouteData | null>(null);
    const [fewChangesPath, setFewChangesPath] = useState<RouteData | null>(
        null,
    );

    const [closedStation, setClosedStation] = useState<string>("");
    const [delayFrom, setDelayFrom] = useState<string>("");
    const [delayTo, setDelayTo] = useState<string>("");
    const [delayTime, setDelayTime] = useState<number>(0);

    // results state
    const [routeData, setRouteData] = useState<RouteData | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    // get which path to show in the UI based on radio button
    const currentRouteData = optimisedRoute ? fastestPath : fewChangesPath;

    useEffect(() => {
        // only search if user has actually typed stations
        if (start !== "" && end !== "") {
            handleSearch();
        }
    }, [optimisedRoute]);

    // form event
    const handleSearch = async () => {
        setIsLoading(true);
        setError(null);
        setRouteData(null);

        // call API
        try {
            const baseParams = {
                start: start,
                end: end,
                station: closedStation,
                delayFrom: delayFrom,
                delayTo: delayTo,
                delayTime: delayTime,
            };

            // call api twice
            const [fastestResponse, fewestResponse] = await Promise.all([
                axios.get<RouteData>(
                    "http://localhost:8080/api/journey/route",
                    {
                        params: { ...baseParams, optimisedRoute: true },
                    },
                ),
                axios.get<RouteData>(
                    "http://localhost:8080/api/journey/route",
                    {
                        params: { ...baseParams, optimisedRoute: false },
                    },
                ),
            ]);

            setFastestPath(fastestResponse.data);
            setFewChangesPath(fewestResponse.data);

            if (closedStation || delayTime > 0) {
                toast.warning("Route calculated with network disruptions!", {
                    position: "top-right",
                    autoClose: 3000,
                    theme: "light",
                });
            }
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                const backendError = err.response?.data as ApiResponse;
                const errorMsg =
                    backendError?.error || "Could not find a route.";
                setError(errorMsg);
                toast.error(errorMsg);
            } else if (err instanceof Error) {
                setError(err.message);
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="container mx-auto p-4 max-w-4xl relative">
            <ToastContainer />
            {/* INPUT FORM */}
            <div className="card bg-base-100 shadow-xl mb-8 border-t-4 border-primary">
                <div className="card-body">
                    <h2 className="card-title text-2xl text-neutral mb-4">
                        Plan Your Journey
                    </h2>

                    <form action={handleSearch} className="flex flex-col gap-4">
                        <div className="flex flex-col md:flex-row gap-4">
                            <input
                                type="text"
                                placeholder="Start Station"
                                className="input outline-0 w-full"
                                value={start}
                                required
                                onChange={(
                                    e: React.ChangeEvent<HTMLInputElement>,
                                ) => setStart(e.target.value)}
                            ></input>

                            <input
                                type="text"
                                placeholder="End Station"
                                className="input outline-0 w-full"
                                value={end}
                                required
                                onChange={(
                                    e: React.ChangeEvent<HTMLInputElement>,
                                ) => setEnd(e.target.value)}
                            ></input>
                        </div>

                        {/* DELAYS MENU */}
                        <div className="collapse collapse-arrow bg-base-200 rounded-box border border-base-300">
                            <input type="checkbox" />
                            <div className="collapse-title text-lg font-bold text-neutral">
                                Network Disruption (Optional)
                            </div>
                            <div className="collapse-content flex flex-col gap-4">
                                <div className="form-control w-full">
                                    <label className="label">
                                        <span className="label-text font-bold">
                                            Close a Station
                                        </span>
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="e.g. Victoria"
                                        className="input outline-0 w-full"
                                        value={closedStation}
                                        onChange={(e) =>
                                            setClosedStation(e.target.value)
                                        }
                                    />
                                </div>

                                <div className="form-control w-full">
                                    <label className="label">
                                        <span className="label-text font-bold">
                                            Add Delay
                                        </span>
                                    </label>
                                    <div className="flex flex-col md:flex-row gap-2">
                                        <input
                                            type="text"
                                            placeholder="From Station"
                                            className="input outline-0 w-full"
                                            value={delayFrom}
                                            onChange={(e) =>
                                                setDelayFrom(e.target.value)
                                            }
                                        />
                                        <input
                                            type="text"
                                            placeholder="To Station"
                                            className="input outline-0 w-full"
                                            value={delayTo}
                                            onChange={(e) =>
                                                setDelayTo(e.target.value)
                                            }
                                        />
                                        <input
                                            type="number"
                                            placeholder="Mins"
                                            className="input outline-0 w-24"
                                            min="0"
                                            value={
                                                delayTime === 0 ? "" : delayTime
                                            }
                                            onChange={(e) =>
                                                setDelayTime(
                                                    parseInt(e.target.value),
                                                )
                                            }
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="flex gap-6 py-2">
                            <div className="form-control">
                                <label className="label cursor-pointer gap-3">
                                    <input
                                        type="radio"
                                        name="optimisation"
                                        className="radio radio-primary"
                                        checked={optimisedRoute === true}
                                        onChange={() => setOptimisedRoute(true)}
                                    />
                                    <span className="label-text font-bold">
                                        Fastest Time
                                    </span>
                                </label>
                            </div>

                            <div className="form-control">
                                <label className="label cursor-pointer gap-3">
                                    <input
                                        type="radio"
                                        name="optimisation"
                                        className="radio radio-primary"
                                        checked={optimisedRoute === false}
                                        onChange={() =>
                                            setOptimisedRoute(false)
                                        }
                                    />
                                    <span className="label-text font-bold">
                                        Fewest Changes
                                    </span>
                                </label>
                            </div>
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary w-full"
                            disabled={isLoading}
                        >
                            {isLoading ? (
                                <span className="loading loading-spinner"></span>
                            ) : (
                                "Find Route"
                            )}
                        </button>
                    </form>
                </div>
            </div>

            {/* DISPLAY RESULTS */}
            {currentRouteData && fastestPath && fewChangesPath && (
                <div className="flex flex-col gap-8 mt-8">
                    <div className="card bg-base-100 shadow-xl border-t-4 border-secondary">
                        <div className="card-body">
                            <div className="flex justify-between items-center border-b pb-4 mb-4">
                                <div>
                                    <h2 className="text-3xl font-bold text-secondary">
                                        {currentRouteData.totalTime} Mins
                                    </h2>
                                    <p className="text-gray-500 font-bold">
                                        Total Journey Time
                                    </p>
                                </div>
                                <div className="text-right">
                                    <h2 className="text-3xl font-bold">
                                        {currentRouteData.totalChanges}
                                    </h2>
                                    <p className="text-gray-500 font-bold">
                                        Total Changes
                                    </p>
                                </div>
                            </div>

                            <ul className="steps steps-vertical">
                                {currentRouteData.path.map((step, index) => {
                                    const isWalking = step.line === "walking";
                                    const stepColor = getStepColorClass(
                                        step.line,
                                    );

                                    return (
                                        <li
                                            key={`${step.station}-${index}`}
                                            className={`step ${stepColor}`}
                                        >
                                            <div className="text-left ml-4">
                                                <p className="font-bold text-lg">
                                                    {step.station}
                                                </p>
                                                <p className="text-sm opacity-70 font-semibold">
                                                    {index === 0
                                                        ? "Start Journey"
                                                        : isWalking
                                                          ? "Walk"
                                                          : `Board ${step.line} line`}
                                                </p>
                                            </div>
                                        </li>
                                    );
                                })}
                            </ul>
                        </div>
                    </div>

                    {/* THE BAR CHART */}
                    <JourneyBarChart path={currentRouteData.path} />

                    {/* THE LINE CHART */}
                    <JourneyLineChart
                        fastestPath={fastestPath.path}
                        fewestPath={fewChangesPath.path}
                    />
                </div>
            )}
        </div>
    );
}
