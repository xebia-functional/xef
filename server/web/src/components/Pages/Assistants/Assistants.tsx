import { useContext, useEffect, useState, ChangeEvent } from "react";
import { useAuth } from "@/state/Auth";
import { LoadingContext } from "@/state/Loading";
import {
  getAssistants,
  postAssistant,
  putAssistant,
  deleteAssistant,
} from "@/utils/api/assistants"; // Asegúrate de que tienes estas funciones en tus utils de API
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

// Asumiendo que tienes un tipo definido para Assistant similar a tu tipo OrganizationResponse
type Assistant = {
  id: number;
  name: string;
  createdAt: string;
};

const emptyAssistant: Assistant = {
  id: 0,
  name: "",
  createdAt: ""
};

export function Assistants() {
  const auth = useAuth();
  const [loading, setLoading] = useContext(LoadingContext);
  const [assistants, setAssistants] = useState<Assistant[]>([]);
  const [showAlert, setShowAlert] = useState<string>('');
  const [selectedAssistant, setSelectedAssistant] = useState<Assistant>(emptyAssistant);
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);

  async function loadAssistants() {
    setLoading(true);
    try {
      const response = await getAssistants(auth.authToken);
      setAssistants(response);
    } catch (error) {
      console.error('Error fetching assistants:', error);
      setShowAlert('Failed to load assistants.');
    }
    setLoading(false);
  }

  useEffect(() => {
    loadAssistants();
  }, []);

  // Agrega aquí las funciones para manejar la creación, edición y eliminación, siguiendo el ejemplo de tu componente de Organizations

  return (
    <Box sx={{ m: 2 }}>
      <Grid container justifyContent="space-between" alignItems="center">
        <Typography variant="h4">Assistants</Typography>
        <Button variant="contained" color="primary" onClick={() => setOpenEditDialog(true)}>
          Create
        </Button>
      </Grid>
      {loading ? (
        <Typography>Loading...</Typography>
      ) : (
        assistants.length === 0 ? (
          <Typography>No assistants available.</Typography>
        ) : (
          <TableContainer component={Paper} sx={{ mt: 3 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Date</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {assistants.map((assistant) => (
                  <TableRow key={assistant.id}>
                    <TableCell>{assistant.createdAt}</TableCell>
                    <TableCell>{assistant.name}</TableCell>
                    <TableCell align="right">
                      <Button onClick={() => {
                        setSelectedAssistant(assistant);
                        setOpenEditDialog(true);
                      }}>Edit</Button>
                      <Button onClick={() => {
                        setSelectedAssistant(assistant);
                        setOpenDeleteDialog(true);
                      }}>Delete</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )
      )}
      {/* Add/Edit Assistant Dialog */}
      {/* Delete Assistant Dialog */}
      {/* Alert Snackbar */}
    </Box>
  );
}
