import { useAuth } from "@/state/Auth";
import { LoadingContext } from "@/state/Loading";
import { getOrganizations } from "@/utils/api/organizations";
import { deleteProjects, getProjects, postProjects, putProjects } from "@/utils/api/projects";
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Grid,
  Paper,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography
} from "@mui/material";
import { ChangeEvent, useContext, useEffect, useState } from "react";

type UpdateProject = {
  id?: number;
  name: string;
  orgId?: number;
}

const emptyUpdateProject = { name: "" }

export function Projects() {

  const auth = useAuth();

  // Alerts

  const [showAlert, setShowAlert] = useState<string>('');

  // functions to open/close the add/edit project dialog

  const [openAddEditProject, setOpenAddEditProject] = useState(false);

  const [projectforUpdating, setProjectForUpdating] = useState<UpdateProject>(emptyUpdateProject);

  const handleClickAddProject = () => {
    setOpenAddEditProject(true);
    setProjectForUpdating(emptyUpdateProject);
  };

  const handleClickEditProject = (org: ProjectResponse) => {
    console.log(org);
    setOpenAddEditProject(true);
    setProjectForUpdating({ ...org, orgId: org.org.id, });
  };

  const handleCloseAddEditProject = () => {
    setOpenAddEditProject(false);
  };

  const handleSaveAddEditProject = async () => {
    if (projectforUpdating.id == null) {
      if (projectforUpdating.orgId == null) {
        setShowAlert("Select an organization");
        throw new Error("orgId is null");
      }
      await postProjects(auth.authToken, { name: projectforUpdating.name, orgId: projectforUpdating.orgId });
    } else {
      await putProjects(auth.authToken, projectforUpdating.id, { name: projectforUpdating.name, orgId: projectforUpdating.orgId });

    }
    loadProjects();
    setOpenAddEditProject(false);
  };

  const nameEditingHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setProjectForUpdating({
      id: projectforUpdating.id,
      name: event.target.value,
      orgId: projectforUpdating.orgId
    })
  };

  const orgEditingHandleChange = (name: String) => {
    const org = organizations.find((org) => org.name == name)
    if (org != undefined) {
      setProjectForUpdating({
        id: projectforUpdating.id,
        name: projectforUpdating.name,
        orgId: org.id
      })
    }
  };

  // functions to open/close the delete organization dialog

  const [openDeleteProject, setOpenDeleteProject] = useState(false);

  const [organizationforDeleting, setProjectForDeleting] = useState<ProjectResponse | undefined>(undefined);

  const handleClickDeleteProject = (org: ProjectResponse) => {
    setOpenDeleteProject(true);
    setProjectForDeleting(org);
  };

  const handleCloseDeleteProject = () => {
    setOpenDeleteProject(false);
  };

  const handleDeleteProject = async () => {
    if (organizationforDeleting != undefined)
      await deleteProjects(auth.authToken, organizationforDeleting.id);

    loadProjects();

    setOpenDeleteProject(false);
  };

  // list of organizations

  const [organizations, setOrganizations] = useState<OrganizationResponse[]>([]);

  async function loadOrganizations() {
    const response = await getOrganizations(auth.authToken);
    setOrganizations(response);
  }

  useEffect(() => {
    loadOrganizations()
  }, []);

  // function to load projects

  const [loading, setLoading] = useContext(LoadingContext);

  const [projects, setProjects] = useState<ProjectResponse[]>([]);

  async function loadProjects() {
    setLoading(true);
    const response = await getProjects(auth.authToken);
    setProjects(response);
    setLoading(false);
  }

  useEffect(() => {
    loadProjects()
  }, []);

  return <>
    {/* List of projects */}
    {loading ?
      <>
        <Typography >
          Loading...
        </Typography>
      </> :
      <>
        <Box sx={{ m: 2 }} />
        <Grid container>
          <Typography variant="h4" gutterBottom>
            Projects
          </Typography>
          <Button
            onClick={handleClickAddProject}
            variant="text"
            disableElevation>
            <Typography variant="button">Add</Typography>
          </Button>
        </Grid>
        <Box sx={{ m: 2 }} />
        <TableContainer component={Paper}>
          <Table sx={{ minWidth: 650 }} aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>Organization</TableCell>
                <TableCell>Name</TableCell>
                <TableCell></TableCell>
                <TableCell></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {projects.map((project) => (
                <TableRow
                  key={project.name}
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell component="th" scope="row" style={{ width: "30%" }}>
                    {project.org.name}
                  </TableCell>
                  <TableCell component="th" scope="row" style={{ width: "60%" }}>
                    {project.name}
                  </TableCell>
                  <TableCell align="right">
                    <Button
                      onClick={() => handleClickEditProject(project)}
                      variant="text"
                      disableElevation>
                      <Typography variant="button">Edit</Typography>
                    </Button>
                  </TableCell>
                  <TableCell align="right">
                    <Button
                      onClick={() => handleClickDeleteProject(project)}
                      variant="text"
                      disableElevation>
                      <Typography variant="button">Delete</Typography>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </>
    }

    {/* Add/Edit Project Dialog */}

    <Dialog open={openAddEditProject} onClose={handleCloseAddEditProject}>
      <DialogTitle>{projectforUpdating.id == null ? "New Project" : "Update Project"}</DialogTitle>
      <DialogContent>
        <Box sx={{ m: 2 }} />
        <Autocomplete
          disablePortal
          onChange={(_: any, newValue: string | null) => {
            if (newValue != null) orgEditingHandleChange(newValue);
          }}
          inputValue={projectforUpdating.orgId == null ? undefined : organizations.find((org) => org.id == projectforUpdating.orgId)?.name ?? undefined}
          id="org"
          options={organizations.map((option) => option.name)}
          sx={{
            width: { xs: '100%', sm: 550 },
          }}
          renderInput={(params) => <TextField {...params}
            label="Organization" />}
        />
        <Box sx={{ m: 2 }} />
        <TextField
          autoFocus
          margin="dense"
          id="name"
          label="Name"
          fullWidth
          variant="standard"
          value={projectforUpdating?.name}
          onChange={nameEditingHandleChange}
          sx={{
            width: { xs: '100%', sm: 550 },
          }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCloseAddEditProject}>Cancel</Button>
        <Button onClick={handleSaveAddEditProject}>{projectforUpdating.id == null ? "Create" : "Update"}</Button>
      </DialogActions>
    </Dialog>

    {/* Delete Project Dialog */}

    <div>
      <Dialog
        open={openDeleteProject}
        onClose={handleCloseDeleteProject}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle>
          {"Delete Project?"}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Are you sure you want to delete this project?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteProject}>No</Button>
          <Button onClick={handleDeleteProject} autoFocus>
            Yes
          </Button>
        </DialogActions>
      </Dialog>
    </div>

    {/* Alert */}
    <Snackbar
      open={!!showAlert}
      onClose={(_, reason) => reason !== 'clickaway' && setShowAlert('')}
      autoHideDuration={5000}>
      <Alert severity="error">{showAlert}</Alert>
    </Snackbar>
  </>;
}
