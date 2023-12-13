This is a [Next.js](https://nextjs.org/) project bootstrapped with [`create-next-app`](https://github.com/vercel/next.js/tree/canary/packages/create-next-app).
It uses [Tailwind CSS](https://tailwindcss.com/) for styling.
And [shadcn](https://ui.shadcn.com/) for some components.

## Getting Started

First create a `.env.local` file in the root directory of the project.
You can copy the `.env.example` file and rename it to `.env.local`.

Then you can install the dependencies:

```bash
bun install
```

Lastly you can run the development server:

```bash
bun dev
```

Or you can build and start the project in production mode:[^1]

[^1]: At the moment this only works if `NEXT_PUBLIC_LOCAL_STORAGE` is set to `false` in the .env.local file.

```bash
bun run build
bun run start
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## Typescript from JSON Schema

This project uses [json-schema-to-typescript](https://github.com/bcherny/json-schema-to-typescript) to generate typescript types from JSON Schema.

```bash
bun install -g json-schema-to-typescript
cd src/main/resources/dx
json2ts -i schema/ -o ../../jchess-web/models/ --cwd schema/types
```

## Generate API Docs

This project uses [AsyncAPI Generator](https://github.com/asyncapi/generator) to generate documentation for the WebSocket Api.  
We also use [Redocly](https://www.npmjs.com/package/@redocly/cli) to generate documentation for the REST Api.  
The output is placed in `/src/main/jchess-web/api-docs/`.

```bash
bun install -g @asyncapi/generator
bun install -g @redocly/cli

# Im /src/main/jchess-web/ Verzeichnis ausführen
ag --output ./api-docs/asyncapi/ ../resources/dx/AsyncApi.schema.yml @asyncapi/html-template
redocly build-docs ../resources/dx/Swagger.schema.yml --output=./api-docs/swagger.html
```

