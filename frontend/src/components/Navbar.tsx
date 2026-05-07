import logo from "../assets/mm-logo.png";
import profile from "../assets/profile.jpg";

export default function Navbar() {
    return (
        <div data-aos="zoom-out" className="navbar bg-base-300 shadow-sm">
            <div className="flex-1 flex items-center gap-2">
                <img
                    src={logo}
                    alt="logo"
                    className="h-8 w-auto object-contain"
                />
                <span className="text-md text-neutral">
                    <span className="font-bold">M</span>anchester{" "}
                    <span className="font-bold">M</span>etrolink
                </span>
            </div>

            {/*Github link*/}
            <div className="flex-none">
                <a href="https://github.com/marstxa">
                    <div className="avatar">
                        <div className="w-10 rounded-full border-2 border-base-200">
                            <img src={profile} alt="profile" />
                        </div>
                    </div>
                </a>
            </div>
        </div>
    );
}
