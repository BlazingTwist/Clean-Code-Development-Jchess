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

Open [http://localhost:3000/web](http://localhost:3000/web) with your browser to see the result.

## Typescript from JSON Schema

This project uses [json-schema-to-typescript](https://github.com/bcherny/json-schema-to-typescript) to generate typescript types from JSON Schema.

```bash
bun install -g json-schema-to-typescript
cd src/main/resources/dx
json2ts -i schema/ -o ../../jchess-web/models/ --cwd schema/types
```

