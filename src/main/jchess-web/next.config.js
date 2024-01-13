const rewrites = () => {
    return [
        {
            source: "/api/resources/:path*",
            destination: `${process.env.NEXT_PUBLIC_JCHESS_UNDERTOW_SERVER_URI}/resources/:path*`
        },
        {
            source: "/api/:path*",
            destination: `${process.env.NEXT_PUBLIC_JCHESS_UNDERTOW_SERVER_URI}/api/:path*`
        }
    ];
}

/** @type {import('next').NextConfig} */
const nextConfig = {
    reactStrictMode: true,
    output: "standalone",
    rewrites
};

module.exports = nextConfig;
