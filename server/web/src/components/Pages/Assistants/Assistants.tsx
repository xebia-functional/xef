import { useContext, useEffect, useState, ChangeEvent } from "react";
import { useAuth } from "@/state/Auth";
import { LoadingContext } from "@/state/Loading";

import {
  getAssistants,
  postAssistant,
  putAssistant,
  deleteAssistant,
} from "@/utils/api/assistants";
import {
  Alert,
    Box,
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    FormControlLabel,
    FormGroup,
    MenuItem,
    Grid,
    Divider,
    Paper,
    Switch,
    Snackbar,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextField,
    Typography,
    Slider
} from "@mui/material";

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
  const [showCreatePanel, setShowCreatePanel] = useState(false);
  const [fileSearchEnabled, setFileSearchEnabled] = useState(false);
  const [codeInterpreterEnabled, setCodeInterpreterEnabled] = useState(false);
  const [JsonObjectEnabled, setJsonObjectEnabled] = useState(false);
  const [temperature, setTemperature] = useState(1);
  const [topP, setTopP] = useState(1);

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

  const models = [
    {
      value: 'gpt-4-turbo',
      label: 'gpt-4-turbo',
    },
    {
      value: 'gpt-4',
      label: 'gpt-4',
    },
    {
      value: 'gpt-3.5-turbo-16k',
      label: 'gpt-3.5-turbo-16k',
    },
    {
      value: 'gpt-3.5-turbo-0125',
      label: 'gpt-3.5-turbo-0125',
    },
    {
      value: 'gpt-3.5-turbo',
      label: 'gpt-3.5-turbo',
    },
    {
       value: 'gpt-3.5-turbo',
       label: 'gpt-3.5-turbo',
    },
  ];

  const handleCreateAssistant = async () => {
    // Aquí se incluirá la lógica para crear un nuevo asistente
    // Por ejemplo: await postAssistant(authToken, { name: selectedAssistant.name });
  };

  const handleFileSearchChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFileSearchEnabled(event.target.checked);
  };

  const handleCodeInterpreterChange = (event: ChangeEvent<HTMLInputElement>) => {
    setCodeInterpreterEnabled(event.target.checked);
  };

  const handleJsonObjectChange = (event: ChangeEvent<HTMLInputElement>) => {
      setJsonObjectEnabled(event.target.checked);
    };

  const handleTemperatureChange = (event: Event, newValue: number | number[]) => {
    setTemperature(newValue as number);
  };
  const handleTopPChange = (event: Event, newValue: number | number[]) => {
      setTopP(newValue as number);
    };
  const handleFilesButtonClick = (toolName: String)=> {
      console.log(`Clicked on ${toolName} button`);
    };

  return (
      <Box sx={{ display: 'flex', height: '100%' }}>
        <Box sx={{ width: showCreatePanel ? '50%' : '100%', transition: '0.3s' }}> </Box>
        {showCreatePanel && (
                <Paper elevation={3} sx={{ p: 2, width: '50%', overflow: 'auto' }}>
                  <Box>
                    <Typography variant="h5" gutterBottom>
                      {selectedAssistant.id ? 'Edit Assistant' : 'Create Assistant'}
                    </Typography>

                    <TextField
                      fullWidth
                      label="Name"
                      value={selectedAssistant.name}
                      placeholder="Enter a user friendly name"
                      onChange={(e) => setSelectedAssistant({ ...selectedAssistant, name: e.target.value })}
                      margin="normal"
                    />
                    <TextField
                      fullWidth
                      label="Instructions"
                      placeholder="You are a helpful assistant"
                      multiline
                      rows={4}
                      margin="normal"
                      // Aquí debes incluir el onChange para actualizar el estado
                    />
                    <TextField
                      fullWidth
                      select
                      label="Model"
                      value={selectedAssistant.model || 'gpt-4-turbo'} // Asegúrate de que 'model' sea parte del estado de 'selectedAssistant'
                      onChange={(e) => setSelectedAssistant({ ...selectedAssistant, model: e.target.value })}
                      margin="normal"
                      sx={{ display: 'block', mt: 3, mb: 3 }}
                      SelectProps={{
                        native: true,
                      }}
                      helperText="Please select your model"
                    >
                      {models.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </TextField >

                    <Typography style={{ fontWeight: 'bold' }}>Tools</Typography>
                    <Divider />
                    <FormControlLabel
                      control={<Switch checked={fileSearchEnabled} onChange={handleFileSearchChange} />}
                      label="File search"
                      sx={{ display: 'block', mt: 2, mb: 2 }}
                    />
                    <Divider />
                    <FormControlLabel
                      control={<Switch checked={codeInterpreterEnabled} onChange={handleCodeInterpreterChange} />}
                      label="Code interpreter"
                      sx={{ display: 'block', mt: 2, mb: 2 }}
                    />
                    <Divider sx={{ display: 'block', mt: 2, mb: 2 }}/>
                    <Typography variant="subtitle1">Functions</Typography>
                    <Divider sx={{ display: 'block', mt: 2, mb: 2 }}/>
                    <Typography  style={{ fontWeight: 'bold' }}>MODEL CONFIGURATION</Typography>
                    <Divider sx={{ display: 'block', mt: 2, mb: 2 }}/>
                    <Typography variant="subtitle1" style={{ fontWeight: 'bold'}}>Response format</Typography>
                    <Divider sx={{ display: 'block', mt: 1, mb: 1 }}/>
                    <FormControlLabel
                      control={<Switch checked={JsonObjectEnabled} onChange={handleJsonObjectChange} />}
                      label="JSON object"
                      sx={{ display: 'block', mt: 1, mb: 1 }}
                    />
                    <Typography gutterBottom>Temperature</Typography>
                    <Slider
                      value={temperature}
                      onChange={handleTemperatureChange}
                      step={0.1}
                      marks
                      min={0}
                      max={1}
                      valueLabelDisplay="auto"
                    />
                    <Typography gutterBottom>Top P</Typography>
                      <Slider
                        value={topP}
                        onChange={handleTopPChange}
                        step={0.1}
                        marks
                        min={0}
                        max={1}
                        valueLabelDisplay="auto"
                      />
                    <Button variant="contained" color="primary" onClick={handleCreateAssistant} sx={{ mt: 2 }}>
                      {selectedAssistant.id ? 'Update' : 'Create'}
                    </Button>
                  </Box>
                </Paper>
              )}
              {/* Vista principal */}
              <Box sx={{ width: showCreatePanel ? '50%' : '100%', transition: '0.3s' }}>
                <Grid container justifyContent="space-between" alignItems="center" sx={{ p: 2 }}>
                  <Typography variant="h4">Assistants</Typography>
                  <Button variant="contained" color="primary" onClick={() => setShowCreatePanel(!showCreatePanel)}>
                    {showCreatePanel ? 'Close' : '+ Create'}
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
                {/* Diálogos de edición y eliminación */}
                {/* Snackbar de alerta */}
              </Box>
            </Box>
          );
        }
