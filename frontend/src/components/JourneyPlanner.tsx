import axios from "axios";
import { useState } from "react";
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

interface ApiErrorResponse {
    error: string;
}

// Falls back to localhost for local dev; set VITE_API_BASE_URL in .env
// for any other environment (staging, production, etc).
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

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
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    // get which path to show in the UI based on radio button.
    // Toggling this never needs a refetch: handleSearch always fetches both
    // options in one go, so switching here just changes which already-fetched
    // result is displayed.
    const currentRouteData = optimisedRoute ? fastestPath : fewChangesPath;

    // form event
    const handleSearch = async (e?: React.FormEvent<HTMLFormElement>) => {
        e?.preventDefault();

        const trimmedStart = start.trim();
        const trimmedEnd = end.trim();

        if (trimmedStart.toLowerCase() === trimmedEnd.toLowerCase()) {
            const msg = "Start and destination stations can't be the same.";
            setError(msg);
            toast.error(msg);
            return;
        }

        setIsLoading(true);
        setError(null);

        // call API
        try {
            const baseParams = {
                start: trimmedStart,
                end: trimmedEnd,
                station: closedStation.trim(),
                delayFrom: delayFrom.trim(),
                delayTo: delayTo.trim(),
                delayTime: delayTime,
            };

            // call api twice, once per route-optimisation strategy
            const [fastestResponse, fewestResponse] = await Promise.all([
                axios.get<RouteData>(
                    `${API_BASE_URL}/api/journey/route`,
                    { params: { ...baseParams, optimisedRoute: true } },
                ),
                axios.get<RouteData>(
                    `${API_BASE_URL}/api/journey/route`,
                    { params: { ...baseParams, optimisedRoute: false } },
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
                const backendError = err.response?.data as ApiErrorResponse;
                const errorMsg =
                    backendError?.error || "Could not find a route.";
                setError(errorMsg);
                toast.error(errorMsg);
            } else if (err instanceof Error) {
                setError(err.message);
                toast.error(err.message);
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleDelayTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.value === "") {
            setDelayTime(0);
            return;
        }
        const parsed = parseInt(e.target.value, 10);
        if (!Number.isNaN(parsed)) {
            setDelayTime(Math.max(0, parsed));
        }
    };

    return (
        <div className="container mx-auto p-4 max-w-4xl relative">
            <ToastContainer />
            {/* INPUT FORM */}
            <div
                data-aos="fade-up"
                className="card bg-base-100 shadow-xl mb-8 border-t-4 border-primary"
            >
                <div className="card-body">
                    <h2 className="card-title text-2xl text-neutral mb-4">
                        Plan Your Journey
                    </h2>

                    <form onSubmit={handleSearch} className="flex flex-col gap-4">
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
                            />

                            <input
                                type="text"
                                placeholder="End Station"
                                className="input outline-0 w-full"
                                value={end}
                                required
                                onChange={(
                                    e: React.ChangeEvent<HTMLInputElement>,
                                ) => setEnd(e.target.value)}
                            />
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
                                            onChange={handleDelayTimeChange}
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

            {error && (
                <div role="alert" className="alert alert-error mb-8">
                    <span>{error}</span>
                </div>
            )}

            {/* DISPLAY RESULTS */}
            {currentRouteData && fastestPath && fewChangesPath && (
                <div data-aos="fade-right" className="flex flex-col gap-8 mt-8">
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