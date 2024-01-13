ARG backend_port

# https://github.com/vercel/next.js/blob/canary/examples/with-docker/Dockerfile
FROM node:18-alpine AS base


# Rebuild the source code only when needed
FROM base AS builder
ARG backend_port
# Check https://github.com/nodejs/docker-node/tree/b4117f9333da4138b03a546ec926ef50a31506c3#nodealpine to understand why libc6-compat might be needed.
RUN apk add --no-cache libc6-compat
WORKDIR /app

COPY jchess-web/package.json jchess-web/package-lock.json* ./
COPY ./jchess-web/src/ src
COPY ./jchess-web/public public
COPY ./jchess-web/.eslintrc.json .eslintrc.json
COPY ./jchess-web/components.json components.json
COPY ./jchess-web/next.config.js next.config.js
COPY ./jchess-web/postcss.config.js postcss.config.js
COPY ./jchess-web/tailwind.config.ts tailwind.config.ts
COPY ./jchess-web/tsconfig.json tsconfig.json
COPY ./resources/dx ./resources/dx

RUN npm i
RUN npm install -g json-schema-to-typescript
RUN json2ts -i ./resources/dx/schema/message -o ./models --cwd ./resources/dx/schema/message

# Next.js collects completely anonymous telemetry data about general usage.
# Learn more here: https://nextjs.org/telemetry
# Uncomment the following line in case you want to disable telemetry during the build.
# ENV NEXT_TELEMETRY_DISABLED 1

# see .env.example
ENV NEXT_PUBLIC_JCHESS_UNDERTOW_SERVER_URI "http://backend:$backend_port"
# verwirrend, aber: http Requests werden auf dem Server aufgelöst, Sockets auf dem Client
# -> PROBLEM: dann stimmt die Addresse hier natürlich nur, wenn man den Server selbst hostet... (TODO erja)
ENV NEXT_PUBLIC_JCHESS_SOCKET_URI "ws://localhost:$backend_port"
ENV NEXT_PUBLIC_BOARD_WITH_COORDINATES "false"
ENV NEXT_PUBLIC_LOCAL_STORAGE "false"

RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production
# Uncomment the following line in case you want to disable telemetry during runtime.
# ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public

# Set the correct permission for prerender cache
RUN mkdir .next
RUN chown nextjs:nodejs .next

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT 3000
# set hostname to localhost
ENV HOSTNAME "0.0.0.0"

# server.js is created by next build from the standalone output
# https://nextjs.org/docs/pages/api-reference/next-config-js/output
CMD ["node", "server.js"]
