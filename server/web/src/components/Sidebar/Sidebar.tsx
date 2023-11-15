import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
} from '@mui/material';
import { HomeRounded } from '@mui/icons-material';

import styles from './Sidebar.module.css';

export type SidebarProps = {
  drawerWidth: number;
  open: boolean;
};

export function Sidebar({ drawerWidth, open }: SidebarProps) {
  return (
    <Box
      component="nav"
      className={styles.container}
      sx={{
        width: { xs: '100%', md: drawerWidth },
        marginLeft: open ? 0 : `-100%`,
      }}>
      <Box
        className={styles.drawer}
        sx={{
          width: {
            xs: '100%',
            md: drawerWidth,
          },
        }}>
        <List>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="/">
              <HomeRounded />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="organizations">
              <ListItemText primary="Organizations" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="projects">
              <ListItemText primary="Projects" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="2">
              <ListItemText primary="Chat" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="generic-question">
              <ListItemText primary="Generic question" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="settings">
              <ListItemText primary="Settings" />
            </ListItemButton>
          </ListItem>
        </List>
      </Box>
    </Box>
  );
}
