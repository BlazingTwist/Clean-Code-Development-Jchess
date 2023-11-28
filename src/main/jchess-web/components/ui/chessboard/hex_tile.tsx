import * as React from "react";
import { cn } from "@/lib/utils";

interface HexTileProps extends React.SVGAttributes<HTMLOrSVGElement> {
    widthPercentage?: number;
    svgClassName?: string;
    pathClassName?: string;
    children?: React.ReactNode;
}

export const HexTile = React.forwardRef<HTMLOrSVGElement, HexTileProps>(
    ({ svgClassName, pathClassName, widthPercentage, children, ...props }, ref) => (
        <div className={`relative pointer-events-none`} style={{ width: `${widthPercentage}%` }}>
            <svg
                className={cn("aspect-[173/200]  relative", svgClassName)}
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 173.20508075688772 200"
                style={{ filter: "drop-shadow(rgba(255, 255, 255, 0.5) 0px 0px)" }}
            >
                <path
                    className={cn(
                        "pointer-events-auto fill-slate-200 hover:fill-red-500 transition-all duration-100 ",
                        pathClassName
                    )}
                    d="M86.60254037844386 0L173.20508075688772 50L173.20508075688772 150L86.60254037844386 200L0 150L0 50Z"
                    {...props}
                ></path>
            </svg>
            <div className="absolute inset-0 flex items-center justify-center">{children}</div>
        </div>
    )
);

