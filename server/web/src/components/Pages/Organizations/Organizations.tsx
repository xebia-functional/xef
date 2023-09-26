import { useAuth } from "@/state/Auth";
import { LoadingContext } from "@/state/Loading";
import { deleteOrganizations, getOrganizations, postOrganizations, putOrganizations } from "@/utils/api/organizations";
import {
  Alert,
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

type UpdateOrganization = {
  id?: number;
  name: string;
}

const emptyUpdateOrganization = { name: "" }

export function Organizations() {

  const auth = useAuth();

  // Alerts

  const [showAlert, setShowAlert] = useState<string>('');

  // functions to open/close the add/edit organization dialog

  const [openAddEditOrganization, setOpenAddEditOrganization] = useState(false);

  const [organizationforUpdating, setOrganizationForUpdating] = useState<UpdateOrganization>(emptyUpdateOrganization);

  const handleClickAddOrganization = () => {
    setOpenAddEditOrganization(true);
    setOrganizationForUpdating(emptyUpdateOrganization);
  };

  const handleClickEditOrganization = (org: OrganizationResponse) => {
    setOpenAddEditOrganization(true);
    setOrganizationForUpdating({ ...org });
  };

  const handleCloseAddEditOrganization = () => {
    setOpenAddEditOrganization(false);
  };

  const handleSaveAddEditOrganization = async () => {
    if (organizationforUpdating.name == "") {
      setShowAlert("Name must not be empty");
      throw new Error("Name must not be emptyl");
    }
    if (organizationforUpdating.id == null) {
      await postOrganizations(auth.authToken, { name: organizationforUpdating.name });
    } else {
      await putOrganizations(auth.authToken, organizationforUpdating.id, { name: organizationforUpdating.name });
    }
    loadOrganizations();

    setOpenAddEditOrganization(false);
  };

  const nameEditingHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setOrganizationForUpdating({
      id: organizationforUpdating.id,
      name: event.target.value,
    });
  };

  // functions to open/close the delete organization dialog

  const [openDeleteOrganization, setOpenDeleteOrganization] = useState(false);

  const [organizationforDeleting, setOrganizationForDeleting] = useState<OrganizationResponse | undefined>(undefined);

  const handleClickDeleteOrganization = (org: OrganizationResponse) => {
    setOpenDeleteOrganization(true);
    setOrganizationForDeleting(org);
  };

  const handleCloseDeleteOrganzation = () => {
    setOpenDeleteOrganization(false);
  };

  const handleDeleteOrganization = async () => {
    if (organizationforDeleting != undefined)
      await deleteOrganizations(auth.authToken, organizationforDeleting.id);

    loadOrganizations();

    setOpenDeleteOrganization(false);
  };

  // function to load organizations

  const [loading, setLoading] = useContext(LoadingContext);

  const [organizations, setOrganizations] = useState<OrganizationResponse[]>([]);

  async function loadOrganizations() {
    setLoading(true);
    const response = await getOrganizations(auth.authToken);
    setOrganizations(response);
    setLoading(false);
  }

  useEffect(() => {
    loadOrganizations()
  }, []);

  return <>
    {/* List of organizations */}
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
            Organizations
          </Typography>
          <Button
            onClick={handleClickAddOrganization}
            variant="text"
            disableElevation>
            <Typography variant="button">Add</Typography>
          </Button>
        </Grid>
        <Box sx={{ m: 2 }} />
        {organizations.length == 0 ?
          <Typography >
            No organizations at this moment
          </Typography> :
          <TableContainer component={Paper}>
            <Table sx={{ minWidth: 650 }} aria-label="simple table">
              <TableHead>
                <TableRow>
                  <TableCell>Users</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell></TableCell>
                  <TableCell></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {organizations.map((organization) => (
                  <TableRow
                    key={organization.name}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                  >
                    <TableCell component="th" scope="row">
                      {organization.users}
                    </TableCell>
                    <TableCell component="th" scope="row" style={{ width: "90%" }}>
                      {organization.name}
                    </TableCell>
                    <TableCell align="right">
                      <Button
                        onClick={() => handleClickEditOrganization(organization)}
                        variant="text"
                        disableElevation>
                        <Typography variant="button">Edit</Typography>
                      </Button>
                    </TableCell>
                    <TableCell align="right">
                      <Button
                        onClick={() => handleClickDeleteOrganization(organization)}
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
        }
      </>
    }

    {/* Add/Edit Organization Dialog */}

    <Dialog open={openAddEditOrganization} onClose={handleCloseAddEditOrganization}>
      <DialogTitle>{organizationforUpdating.id == null ? "New Organization" : "Update Organization"}</DialogTitle>
      <DialogContent>
        <TextField
          autoFocus
          margin="dense"
          id="name"
          label="Name"
          fullWidth
          variant="standard"
          value={organizationforUpdating?.name}
          onChange={nameEditingHandleChange}
          sx={{
            width: { xs: '100%', sm: 550 },
          }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCloseAddEditOrganization}>Cancel</Button>
        <Button onClick={handleSaveAddEditOrganization}>{organizationforUpdating.id == null ? "Create" : "Update"}</Button>
      </DialogActions>
    </Dialog>

    {/* Delete Organization Dialog */}

    <div>
      <Dialog
        open={openDeleteOrganization}
        onClose={handleCloseDeleteOrganzation}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle>
          {"Delete Organizaction?"}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Are you sure you want to delete this organization?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteOrganzation}>No</Button>
          <Button onClick={handleDeleteOrganization} autoFocus>
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
