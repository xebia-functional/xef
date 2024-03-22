import { Box } from '@mui/material';

import logo from '@/assets/xef-brand-name-white.svg';

import styles from './Footer.module.css';

export function Footer() {
  return (
    <footer className={styles.footer}>
      <Box component="span" className={styles.logoContainer}>
        Powered by
        <a href="https://xef.ai" target="_blank" rel="noopener noreferrer">
          <img src={logo} alt="xef.ai logo" className={styles.logo} />
        </a>
      </Box>
    </footer>
  );
}
