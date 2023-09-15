import { useAuth } from "@/state/Auth";
import { LoadingContext } from "@/state/Loading";
import { PostOrganizationProps, getOrganizations, postOrganizations, putOrganizations } from "@/utils/api/organizations";
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Paper,
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

export function Organizations() {

  const auth = useAuth();

  // functions to open/close the add/edit organization dialog

  const [openAddEditOrganization, setOpenAddEditOrganization] = useState(false);

  const [organizationforUpdating, setOrganizationForUpdating] = useState<OrganizationResponse | undefined>(undefined); // when adding is undefined and updating has the organization

  const [organizationDataInDialog, setOrganizationDataInDialog] = useState<PostOrganizationProps | undefined>(undefined);

  const handleClickAddOrganization = () => {
    setOpenAddEditOrganization(true);
    setOrganizationForUpdating(undefined);
    setOrganizationDataInDialog(undefined);
  };

  const handleClickEditOrganization = (org: OrganizationResponse) => {
    setOpenAddEditOrganization(true);
    setOrganizationForUpdating(org);
    setOrganizationDataInDialog({ name: org.name })
  };

  const handleCloseAddEditOrganization = () => {
    setOpenAddEditOrganization(false);
  };

  const handleSaveAddEditOrganization = async () => {
    if (organizationDataInDialog != undefined) {
      if (organizationforUpdating == undefined)
        await postOrganizations(auth.authToken, organizationDataInDialog);
      else
        await putOrganizations(auth.authToken, organizationforUpdating.id, organizationDataInDialog);

      loadOrganizations();
    }
    setOpenAddEditOrganization(false);
  };

  const nameEditingHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setOrganizationDataInDialog({ name: event.target.value })
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
        <TableContainer component={Paper}>
          <Table sx={{ minWidth: 650 }} aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>Users</TableCell>
                <TableCell>Name</TableCell>
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
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </>
    }

    {/* Add/Edit Organization Dialog */}

    <Dialog open={openAddEditOrganization} onClose={handleCloseAddEditOrganization}>
      <DialogTitle>{organizationforUpdating == undefined ? "New Organization" : "Update Organization"}</DialogTitle>
      <DialogContent>
        <TextField
          autoFocus
          margin="dense"
          id="name"
          label="Name"
          fullWidth
          variant="standard"
          value={organizationDataInDialog?.name}
          onChange={nameEditingHandleChange}
          sx={{
            width: { xs: '100%', sm: 550 },
          }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCloseAddEditOrganization}>Cancel</Button>
        <Button onClick={handleSaveAddEditOrganization}>{organizationforUpdating == undefined ? "Create" : "Update"}</Button>
      </DialogActions>
    </Dialog>
  </>;
}
