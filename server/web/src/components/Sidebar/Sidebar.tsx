import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
} from '@mui/material';
import organizationsIcon from '../../assets/organizations-icon.svg';
import assistantsIcon from '../../assets/assistants-icon.svg';
import projectsIcon from '../../assets/projects-icon.svg';
import chatIcon from '../../assets/chat-icon.svg';
import genericQuestionIcon from '../../assets/generic-question-icon.svg';
import settingsIcon from '../../assets/settings-icon.svg';
import homeIcon from '../../assets/home-icon.svg';

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
            <ListItemButton component={RouterLink} to="/" className={styles.buttons}>
              <img src={homeIcon} alt="Home" className={styles.marginRightIcons} />
              <ListItemText primary="Home" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="organizations" className={styles.buttons}>
               <img src={organizationsIcon} alt="Organizations" className={styles.marginRightIcons} />
              <ListItemText primary="Organizations" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="/assistants" className={styles.buttons}>
               <img src={assistantsIcon} alt="Assistants" className={styles.marginRightIcons} />
              <ListItemText primary="Assistants" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="projects" className={styles.buttons}>
              <img src={projectsIcon} alt="Projects" className={styles.marginRightIcons} />
              <ListItemText primary="Projects" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="2" className={styles.buttons}>
               <img src={chatIcon} alt="Chat" className={styles.marginRightIcons} />
              <ListItemText primary="Chat" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="generic-question" className={styles.buttons}>
              <img src={genericQuestionIcon} alt="Generic question" className={styles.marginRightIcons} />
              <ListItemText primary="Generic question" />
            </ListItemButton>
          </ListItem>
          <ListItem disablePadding>
            <ListItemButton component={RouterLink} to="settings" className={styles.buttons}>
              <img src={settingsIcon} alt="Settings" className={styles.marginRightIcons} />
              <ListItemText primary="Settings" />
            </ListItemButton>
          </ListItem>
        </List>
      </Box>
    </Box>
  );
}
