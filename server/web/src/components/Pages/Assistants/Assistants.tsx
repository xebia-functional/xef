import { useContext, useEffect, useState, ChangeEvent } from "react";
import { useAuth } from "@/state/Auth";
import { LoadingContext } from "@/state/Loading";
import styles from './Assistants.module.css';
import { getAssistants } from '@/utils/api/assistants';
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
  Grid,
  Divider,
  Paper,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Slider,
  MenuItem,
  Switch
} from "@mui/material";

type AssistantToolsCode = {
  type: 'code_interpreter';
};

type FunctionObject = {
  name: string;
  description: string;
  parameters: Record<string, string>;
};

type AssistantToolsFunction = {
  type: 'function';
  function: FunctionObject;
};

type AssistantToolsRetrieval = {
  type: 'retrieval';
};

type AssistantObjectToolsInner = AssistantToolsCode | AssistantToolsFunction | AssistantToolsRetrieval;

type AssistantObject = {
  id: string;
  object: 'assistant';
  createdAt: number;
  name?: string;
  description?: string;
  model: string;
  instructions?: string;
  tools: AssistantObjectToolsInner[];
  fileIds: string[];
  metadata: Record<string, string> | null;
};

const emptyAssistant: AssistantObject = {
  id: "",
  object: 'assistant',
  createdAt: 0,
  model: "",
  tools: [],
  fileIds: [],
  metadata: null
};

export function Assistants() {
  const auth = useAuth();
  const [loading, setLoading] = useContext(LoadingContext);
  const [assistants, setAssistants] = useState<AssistantObject[]>([]);
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
  const [assistantCreated, setAssistantCreated] = useState(false);

  const [fileSearchSelectedFile, fileSearchSetSelectedFile] = useState<File[]>([]);
  const [fileSearchDialogOpen, setFileSearchDialogOpen] = useState(false);
  const [codeInterpreterSelectedFile, codeInterpreterSetSelectedFile] = useState<File[]>([]);
  const [codeInterpreterDialogOpen, setCodeInterpreterDialogOpen] = useState(false);

  const [fileSearchFiles, setFileSearchFiles] = useState<{ name: string; size: number; uploaded: string }[]>([]);
  const [attachedFilesFileSearch, setAttachedFilesFileSearch] = useState<{ name: string; size: string; uploaded: string }[]>([]);
  const [attachedFilesCodeInterpreter, setAttachedFilesCodeInterpreter] = useState<{ name: string; size: string; uploaded: string }[]>([]);

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

  const handleTemperatureInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseFloat(event.target.value);
    if (!isNaN(value)) {
      setTemperature(value);
    }
  };

  const handleTopPInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseFloat(event.target.value);
    if (!isNaN(value)) {
      setTopP(value);
    }
  };

  const handleFileSearchDialogClose = () => {
    setFileSearchDialogOpen(false);
    setAttachedFilesFileSearch(fileSearchSelectedFile);
  };

  const handleCodeInterpreterDialogClose = () => {
    setCodeInterpreterDialogOpen(false);
    setAttachedFilesCodeInterpreter(codeInterpreterSelectedFile);
  };

  const handleFileSearchButtonClick = () => {
    setFileSearchDialogOpen(true);
  };

  const handleCodeInterpreterButtonClick = () => {
    setCodeInterpreterDialogOpen(true);
  };

  const handleFileSearchInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files);
    const newFiles = files.map((file) => ({
      name: file.name,
      size: `${Math.round(file.size / 1024)} KB`,
      uploaded: new Date().toLocaleString()
    }));
    fileSearchSetSelectedFile((prevFiles) => [...prevFiles, ...newFiles]);
  };

  const handleCodeInterpreterInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files);
    const newFiles = files.map((file) => ({
      name: file.name,
      size: `${Math.round(file.size / 1024)} KB`,
      uploaded: new Date().toLocaleString()
    }));
    codeInterpreterSetSelectedFile((prevFiles) => [...prevFiles, ...newFiles]);
  };

  const handleDeleteFile = (index: number) => {
    const updatedFiles = fileSearchSelectedFile.filter((_, i) => i !== index);
    fileSearchSetSelectedFile(updatedFiles);
  };

  const openFileSelector = (handleInputChange) => {
    const input = document.createElement("input");
    input.type = "file";
    input.multiple = true;
    input.onchange = (event) => {
      const files = Array.from(event.target.files);
      handleInputChange(event);
    };
    input.click();
  };

  const handleCreateAssistant = async () => {

  };

  const models = [
    { value: 'gpt-4o-2024-05-13', label: 'gpt-4o-2024-05-13' },
    { value: 'gpt-3.5-turbo', label: 'gpt-3.5-turbo' },
    { value: 'gpt-4o', label: 'gpt-4o' },
    { value: 'gpt-4-vision-preview', label: 'gpt-4-vision-preview' },
    { value: 'gpt-4-turbo-preview', label: 'gpt-4-turbo-preview' },
    { value: 'gpt-4-2024-04-09', label: 'gpt-4-2024-04-09' },
    { value: 'gpt-4-turbo', label: 'gpt-4-turbo' },
    { value: 'gpt-4-1106-preview', label: 'gpt-4-1106-preview' },
    { value: 'gpt-4-0613', label: 'gpt-4-0613' },
    { value: 'gpt-4-0125-preview', label: 'gpt-4-0125-preview' },
    { value: 'gpt-4', label: 'gpt-4' },
    { value: 'gpt-3.5-turbo-16k-0613', label: 'gpt-3.5-turbo-16k-0613' },
    { value: 'gpt-3.5-turbo-16k', label: 'gpt-3.5-turbo-16k' },
    { value: 'gpt-3.5-turbo-1106', label: 'gpt-3.5-turbo-1106' },
    { value: 'gpt-3.5-turbo-0613', label: 'gpt-3.5-turbo-0613' },
    { value: 'gpt-3.5-turbo-0125', label: 'gpt-3.5-turbo-0125' },
  ];

  const largeDialogStyles = {
    minWidth: '500px',
    textAlign: 'left',
  };

async function loadAssistants() {
  setLoading(true);
  try {
    if (auth.token) {
      console.log('auth.token:', auth.token); // Log auth.token
      const response = await getAssistants(auth.token);
      console.log('Full API response:', response); // Log full API response
      if (response.data) {
        setAssistants(response.data);
      } else {
        console.error('No data in API response');
      }
    } else {
      console.error('auth.token is undefined');
    }
  } catch (error) {
    console.error(error);
  } finally {
    setLoading(false);
  }
}

useEffect(() => {
  if (auth.token) {
    loadAssistants();
  }
}, [auth.token]);

//only to see the state of assistants, then im going to delete it
useEffect(() => {
  console.log('assistants:', assistants);
}, [assistants]);

  return (
        <Box className={styles.container}>

                <Box sx={{ flexGrow: 1, display: 'flex', gap: 0 }}>

                  {/* Container of Assistants */}
                  <Grid container justifyContent="center" alignItems="flex-start" sx={{ p: 2, width: '30%', height: '100%' }}>
                    <div style={{ width: '85%', textAlign: 'center' }}>
                      <Typography variant="h4">Assistants</Typography>
                      {loading ? (
                        <Typography>Loading...</Typography>
                      ) : (
                        assistants.length === 0 && !assistantCreated ? (
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
                                    <TableCell>{assistant.id}</TableCell>
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
                    </div>
                  </Grid>

              <Divider orientation="vertical" flexItem sx={{ height: '100vh' }} />

              {/* Container of Create Assistant */}
              <Grid container justifyContent="center" alignItems="flex-start" sx={{ p: 2, width: '70%', height: '100%'}}>
                <div style={{ width: '75%', textAlign: 'center', width: showCreatePanel ? '75%' : '30%', transition: '0.3s'}}>

                  {/* Creation panel */}
                  {showCreatePanel && (
                    <Paper elevation={3} sx={{ p: 2, width: '100%', overflow: 'auto' }}>
                      <Box sx={{ paddingLeft: '20px', paddingRight: '20px' }}>
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
                        />
                        <TextField
                          fullWidth
                          select
                          label="Model"
                          value={selectedAssistant.model || 'gpt-4-turbo'}
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
                        <Typography className={styles.boldText}>Tools</Typography>
                        <Divider />
                        <div style={{ display: 'flex', alignItems: 'center' }}>
                              <FormControlLabel
                                control={<Switch checked={fileSearchEnabled} onChange={handleFileSearchChange} />}
                                label="File search"
                                sx={{ flex: '1', mt: 2, mb: 2 }}
                              />
                              <Button onClick={handleFileSearchButtonClick}>+ Files</Button>
                            </div>
                            {fileSearchDialogOpen && (
                              <Dialog
                                open={fileSearchDialogOpen}
                                onClose={handleFileSearchDialogClose}
                                maxWidth="lg"
                                PaperProps={{ sx: largeDialogStyles }}
                                onDragOver={(e) => {
                                    e.preventDefault();
                                  }}
                                  onDragEnter={(e) => {
                                    e.preventDefault();
                                  }}
                                  onDrop={(e) => {
                                    e.preventDefault();
                                    const files = Array.from(e.dataTransfer.files);
                                    handleFileSearchInputChange({ target: { files } });
                                  }}
                              >
                                <DialogTitle>Attach files to file search</DialogTitle>
                                <DialogContent>
                                  <Typography variant="body1" sx={{ textAlign: 'center', fontWeight: 'bold', margin:3}}>
                                    {fileSearchSelectedFile.length === 0 ? (
                                      <>
                                        Drag your files here or{" "}
                                            <span
                                              style={{
                                                color: "blue",
                                                cursor: "pointer",
                                                transition: "color 0.3s ease",
                                              }}
                                              onClick={(e) => {
                                                e.stopPropagation();
                                                openFileSelector(handleFileSearchInputChange);
                                              }}
                                              onMouseEnter={(e) => {
                                                e.target.style.color = "darkblue";
                                              }}
                                              onMouseLeave={(e) => {
                                                e.target.style.color = "blue";
                                              }}
                                            >
                                              click to upload
                                            </span>
                                        <Typography variant="body2" sx={{ marginTop: 1 }}>
                                              Information in attached files will be available to this assistant.
                                        </Typography>
                                      </>
                                    ) : (
                                      <> </>
                                    )}
                                  </Typography>

                                  {fileSearchSelectedFile.length > 0 && (
                                     <TableContainer>
                                        <Table>
                                        <TableHead>
                                          <TableRow>
                                            <TableCell>File</TableCell>
                                            <TableCell>Size</TableCell>
                                            <TableCell>Uploaded</TableCell>
                                          </TableRow>
                                        </TableHead>
                                        <TableBody>
                                          {fileSearchSelectedFile.map((file, index) => (
                                            <TableRow key={index}>
                                              <TableCell>{file.name}</TableCell>
                                              <TableCell>{file.size}</TableCell>
                                              <TableCell>{file.uploaded}</TableCell>
                                              <TableCell>
                                                <Button onClick={() => handleDeleteFile(index)}>
                                                  Delete
                                                </Button>
                                              </TableCell>
                                            </TableRow>
                                          ))}
                                        </TableBody>
                                      </Table>
                                    </TableContainer>
                                  )}
                                </DialogContent>

                                <Divider sx={{ width: '90%', margin: 'auto' }} />
                                <DialogActions>
                                  {fileSearchSelectedFile.length > 0 && (
                                        <Button onClick={() => openFileSelector(handleFileSearchInputChange)}>Add</Button>
                                  )}
                                  <Button onClick={handleFileSearchDialogClose}>Cancel</Button>
                                  <Button onClick={handleFileSearchDialogClose}
                                          disabled={fileSearchSelectedFile.length === 0}
                                          color="primary">Attach</Button>
                                </DialogActions>
                              </Dialog>

                            )}
                        {attachedFilesFileSearch.map((file, index) => (
                          <Typography
                            key={index}
                            sx={{ textAlign: 'left', fontSize: '0.9rem' }}
                          >
                             File {index + 1}: {file.name}
                          </Typography>
                        ))}
                        <Divider />
                        <div style={{ display: 'flex', alignItems: 'center' }}>
                          <FormControlLabel
                            control={<Switch checked={codeInterpreterEnabled} onChange={handleCodeInterpreterChange} />}
                            label="Code interpreter"
                            sx={{ display: 'block', mt: 2, mb: 2 }}
                          />
                          <Button onClick={handleCodeInterpreterButtonClick} sx={{ ml: 'auto' }}>+ Files</Button>
                        </div>

                       {codeInterpreterDialogOpen && (
                           <Dialog
                             open={codeInterpreterDialogOpen}
                             onClose={handleCodeInterpreterDialogClose}
                             maxWidth="lg"
                             PaperProps={{ sx: largeDialogStyles }}
                             onDragOver={(e) => {
                                 e.preventDefault();
                               }}
                               onDragEnter={(e) => {
                                 e.preventDefault();
                               }}
                               onDrop={(e) => {
                                 e.preventDefault();
                                 const files = Array.from(e.dataTransfer.files);
                                 handleCodeInterpreterInputChange({ target: { files } });
                               }}
                           >
                             <DialogTitle>Attach files to file search</DialogTitle>
                             <DialogContent>
                               <Typography variant="body1" sx={{ textAlign: 'center', fontWeight: 'bold' }}>
                                 {codeInterpreterSelectedFile.length === 0 ? (
                                   <>
                                     Drag your files here or{" "}
                                     <span
                                       style={{
                                         color: "blue",
                                         cursor: "pointer",
                                         transition: "color 0.3s ease",
                                       }}
                                       onClick={(e) => {
                                         e.stopPropagation();
                                         openFileSelector(handleCodeInterpreterInputChange);
                                       }}
                                       onMouseEnter={(e) => {
                                         e.target.style.color = "darkblue";
                                       }}
                                       onMouseLeave={(e) => {
                                         e.target.style.color = "blue";
                                       }}
                                     >
                                       click to upload
                                     </span>
                                     <Typography variant="body2" sx={{ marginTop: 1 }}>
                                           Information in attached files will be available to this assistant.
                                     </Typography>
                                   </>
                                 ) : (
                                   <> </>
                                 )}
                               </Typography>

                               {codeInterpreterSelectedFile.length > 0 && (
                                 <TableContainer>
                                   <Table>
                                     <TableHead>
                                       <TableRow>
                                         <TableCell>File</TableCell>
                                         <TableCell>Size</TableCell>
                                         <TableCell>Uploaded</TableCell>
                                       </TableRow>
                                     </TableHead>
                                     <TableBody>
                                       {codeInterpreterSelectedFile.map((file, index) => (
                                         <TableRow key={index}>
                                           <TableCell>{file.name}</TableCell>
                                           <TableCell>{file.size}</TableCell>
                                           <TableCell>{file.uploaded}</TableCell>
                                           <TableCell>
                                             <Button onClick={() => handleDeleteFile(index)}>
                                               Delete
                                             </Button>
                                           </TableCell>
                                         </TableRow>
                                       ))}
                                     </TableBody>
                                   </Table>
                                 </TableContainer>
                               )}
                             </DialogContent>

                             <Divider sx={{ width: '90%', margin: 'auto' }} />
                             <DialogActions>
                               {codeInterpreterSelectedFile.length > 0 && (
                                     <Button onClick={() => openFileSelector(handleCodeInterpreterInputChange)}>Add</Button>
                               )}
                               <Button onClick={handleCodeInterpreterDialogClose}>Cancel</Button>
                               <Button onClick={handleCodeInterpreterDialogClose}
                                       disabled={codeInterpreterSelectedFile.length === 0}
                                       color="primary">Attach</Button>
                             </DialogActions>
                           </Dialog>
                         )}
                     {attachedFilesCodeInterpreter.map((file, index) => (
                       <Typography
                         key={index}
                         sx={{ textAlign: 'left', fontSize: '0.9rem' }}
                       >
                          File {index + 1}: {file.name}
                       </Typography>
                     ))}
                        <Divider />
                        <div style={{ display: 'flex', alignItems: 'center' }}>
                          <Typography variant="subtitle1">Functions</Typography>
                          <Button sx={{ ml: 'auto' }}>+ Functions</Button>
                        </div>
                        <Divider sx={{ marginBottom: 6 }} />

                        <div style={{ textAlign: 'left' }}>
                          <Typography className={styles.boldText}>MODEL CONFIGURATION</Typography>
                          <Divider sx={{ display: 'block', mt: 2, mb: 2 }}/>
                          <Typography variant="subtitle1" className={styles.boldText}>Response format</Typography>

                          <FormControlLabel
                            control={<Switch checked={JsonObjectEnabled} onChange={handleJsonObjectChange} />}
                            label="JSON object"
                            sx={{ display: 'block', mt: 1, mb: 3 }}
                          />
                        </div>

                        <div>
                          <div className={styles.sliders}>
                            <Typography gutterBottom>Temperature</Typography>
                            <TextField
                              value={temperature}
                              onChange={handleTemperatureInputChange}
                              type="number"
                              InputProps={{
                                inputProps: {
                                  min: 0,
                                  max: 2,
                                  step: 0.01,
                                  inputMode: 'numeric'
                                },
                                sx: { '& input': { padding: '4px 7px' } }
                              }}
                            />
                          </div>
                          <Slider
                            value={temperature}
                            onChange={handleTemperatureChange}
                            step={0.01}
                            marks
                            min={0}
                            max={2}
                            valueLabelDisplay="auto"
                          />
                          <div className={styles.sliders}>
                            <Typography gutterBottom>Top P</Typography>
                            <TextField
                              value={topP}
                              onChange={handleTopPInputChange}
                              type="number"
                              InputProps={{
                                inputProps: {
                                  min: 0,
                                  max: 1,
                                  step: 0.01,
                                  inputMode: 'numeric'
                                },
                                sx: { '& input': { padding: '4px 7px' } }
                              }}
                            />
                          </div>
                          <Slider
                            value={topP}
                            onChange={handleTopPChange}
                            step={0.01}
                            marks
                            min={0}
                            max={1}
                            valueLabelDisplay="auto"
                          />
                        </div>

                        <div style={{ textAlign: 'right', marginTop: '2px' }}>
                          <Button variant="contained" color="primary" onClick={handleCreateAssistant}>
                            {selectedAssistant.id ? 'Update' : 'Create'}
                          </Button>
                        </div>
                      </Box>
                    </Paper>
                  )}
                </div>
              </Grid>
            </Box>

            <Button
              variant="contained"
              color="primary"
              onClick={() => setShowCreatePanel(!showCreatePanel)}
              sx={{ position: 'absolute', top: '10px', right: '10px', zIndex: 1 }}
            >
              {showCreatePanel ? 'Close' : '+ Create'}
            </Button>

          </Box>
      );
    }