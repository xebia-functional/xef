import { isRouteErrorResponse, useRouteError } from 'react-router-dom';

import styles from './ErrorPage.module.css';

export function ErrorPage() {
  const error = useRouteError();
  console.error(error);

  const message = isRouteErrorResponse(error)
    ? `${error.status} - ${error.statusText}`
    : error instanceof Error
    ? error.message
    : 'Unknown Error';

  return (
    <div id="error-page" className={styles.container}>
      <h1>Oops!</h1>
      <p>Sorry, an unexpected error has occurred.</p>
      <p>
        <i>{message}</i>
      </p>
    </div>
  );
}
