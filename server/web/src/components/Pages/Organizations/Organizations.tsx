import { LoadingContext } from "@/state/Loading";
import {
  Box,
  Button,
  Grid,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography
} from "@mui/material";
import { useContext, useEffect, useState } from "react";

export function Organizations() {

  const [loading, setLoading] = useContext(LoadingContext);

  const [organizations, setOrganizations] = useState<OrganizationsResponse>({ items: [] });

  async function loadOrganizations() {
    setLoading(true);
    //const organizations = await getOrganizations();
    const response: OrganizationsResponse = { items: [{ id: 1, name: "test" }] }

    setOrganizations(response);
    setLoading(false);
    return Promise.resolve();
  }

  useEffect(() => {
    loadOrganizations()
  });


  //loadOrganizations();

  return loading ?
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
              <TableCell>Name</TableCell>
              <TableCell></TableCell>
              <TableCell></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {organizations.items.map((organization) => (
              <TableRow
                key={organization.name}
                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
              >
                <TableCell component="th" scope="row" style={{ width: "90%" }}>
                  {organization.name}
                </TableCell>
                <TableCell align="right">
                  <Button
                    variant="text"
                    disableElevation>
                    <Typography variant="button">Edit</Typography>
                  </Button>
                </TableCell>
                <TableCell align="right">
                  <Button
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
    </>;
}
