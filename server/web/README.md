# Xef Dashboard

The dashboard and admin panel for the xef-server module and other AI xef powered initiatives.


## Development

The project is built with [vite](https://vitejs.devs), a build tool that aims to provide a faster and leaner development experience for modern web projects.

One of vite goals is to let the developer focus on the development and features instead of spending time in setting the dev environment. Should you need it, vite is fully documented and offers a rich and detailed guide to solve any doubt in [their site](https://vitejs.dev/guide/).

Through vite, you can currently run the following commands in the project:

- Start a dev server to work on the project:
  ```bash
  npm run dev
  ```

- Build the project for production:
  ```bash
  npm run build
  ```

- Locally preview a production build:
  ```bash
  npm run preview
  ```

You have different options for these commands in the [vite web CLI section](https://vitejs.dev/guide/cli.html).


### Features

The vite build process supports:

- [TypeScript](https://vitejs.dev/guide/features.html#typescript)
- [JSX](https://vitejs.dev/guide/features.html#jsx)
- [Hot module replacement](https://vitejs.dev/guide/features.html#hot-module-replacement)
- [Different styling solutions](https://vitejs.dev/guide/features.html#css).
- [Static assets management](https://vitejs.dev/guide/features.html#static-assets).
- [WASM](https://vitejs.dev/guide/features.html#webassembly)
- [Web workers](https://vitejs.dev/guide/features.html#web-workers)


### Environment

You need to have Node.js versions 14.18+ or 16+. But the current LTS is [Node.js 18](https://nodejs.org), so it is recommended to go with that one.


### UI library

The project relies on [React](https://react.dev/) and [MUI](https://mui.com/) to build the features and all its visual components.

Specifically, the project uses the MUI Core - Material UI components. They are ready-to-use foundational React components and are detailed and extensively documented in the [MUI site](https://mui.com/material-ui/getting-started/).


### Routing

Client side routing is managed through [React Router](https://reactrouter.com) To add new routes or modify the current ones, follow the settings in the `src/main.tsx` file.


### State management

There is no specific state management library in the app.

Follow React recommendations to use state hooks when you are gonna set some kind of state for a single (or app branch) component, and contexts to share state between different sections in the page.

These two approaches can be found in the sidebar (local state), and in the use of a loading state (context).


### Styling

Beyond the MUI inner styling engine, the project uses [CSS modules](https://github.com/css-modules/css-modules) to style the components. You can find a `.module.css` file along the `tsx` one.


### Code linting

Thorugh [ESLint](https://eslint.org/) and [Prettier](https://prettier.io/), you are gonna find different rules already configured and being in use in the project. Unless you want to modify any of them, you shouldn't need to set up anything as you probably already use a modern IDE/code editor like [VS Code](https://code.visualstudio.com/).

### Code organization

- Most of the configuration and option files are placed at the project root.
- Static assets can be found in the `public` directory.
- Source code for the app is under `src`.
- With this in mind, other assets that would be processed through vite are stored in the `src/assets`.
- Then you can also find `components`. Each component includes a TypeScript file, a styling one, and an _index_, for a cleaner exporting/importing.
- Code concerning state and other contexts related functions are in `state`.
- The MUI theme definition and configuration is set in the `styles` folder. You might not need to touch any of these settings. 
- Other utility and helper functions shpuld be placed under  `src/utils`.

