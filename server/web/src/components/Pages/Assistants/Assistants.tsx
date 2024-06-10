import { useContext, useEffect, useState, ChangeEvent } from "react";
import { LoadingContext } from "@/state/Loading";
import styles from './Assistants.module.css';
import { getAssistants, getAssistantById, postAssistant, deleteAssistant, putAssistant } from '@/utils/api/assistants';

import { SettingsContext } from '@/state/Settings';
import React from 'react';
import moment from 'moment';
import infoIcon from '../../../assets/info-icon.svg';
import deleteIcon from '../../../assets/delete-icon.svg';
import { Tooltip } from '@mui/material';

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
  List,
  ListItem,
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

type CreateAssistantRequestToolResourcesFileSearch = {
  vectorStoreIds: string[];
};

type CreateAssistantRequestToolResourcesCodeInterpreter = {
  fileIds?: string[];
};

type CreateAssistantRequestToolResources = {
  codeInterpreter?: CreateAssistantRequestToolResourcesCodeInterpreter;
  fileSearch?: CreateAssistantRequestToolResourcesFileSearch;
};

type CreateAssistantRequest = {
   model: string;
   name?: string;
   description?: string;
   instructions?: string;
   tools?: AssistantObjectToolsInner[];
   toolResources?: CreateAssistantRequestToolResources | null;
   metadata?: Record<string, string> | null;
   temperature?: number;
   top_p?: number;
   responseFormat?: any | null;
 }

 type ModifyAssistantRequest = {
    model: string;
    name?: string;
    description?: string;
    instructions?: string;
    tools?: AssistantObjectToolsInner[];
    toolResources?: CreateAssistantRequestToolResources | null;
    metadata?: Record<string, string> | null;
    temperature?: number;
    top_p?: number;
    responseFormat?: any | null;
  }

const emptyAssistantObject: AssistantObject = {
    id: "",
    object: "assistant",
    createdAt: 0,
    name: "",
    description: "",
    model: "",
    instructions: "",
    tools: [],
    metadata: null,
    toolResources: null,
    temperature: 1,
    top_p: 1,
    responseFormat: null
};

const emptyAssistantRequest: CreateAssistantRequest = {
  name: '',
  instructions: '',
  model: '',
  temperature: 1,
  top_p: 1,
};

const emptyModifyAssistantRequest: ModifyAssistantRequest = {
  name: '',
  instructions: '',
  model: '',
  temperature: 1,
  top_p: 1,
};

export function Assistants() {
  const [loading, setLoading] = useContext(LoadingContext);
  const [assistants, setAssistants] = useState<AssistantObject[]>([]);
  const [showAlert, setShowAlert] = useState<string>('');
  const [selectedAssistant, setSelectedAssistant] = useState<AssistantObject>(emptyAssistantObject);
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [showCreatePanel, setShowCreatePanel] = useState(false);
  const [fileSearchEnabled, setFileSearchEnabled] = useState(false);
  const [codeInterpreterEnabled, setCodeInterpreterEnabled] = useState(false);
  const [JsonObjectEnabled, setJsonObjectEnabled] = useState(false);
  const [createdAssistant, setCreatedAssistant] = useState<CreateAssistantRequest>(emptyAssistantRequest);
  const [editedAssistant, setEditedAssistant] = useState<ModifyAssistantRequest>(emptyModifyAssistantRequest);

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

  const handleTemperatureChange = (event: Event, newValue: number | number[], id: string | null = null) => {
    const value = newValue as number;
    if (id) {
      setSelectedAssistant((prev) => ({ ...prev, temperature: value }));
    } else {
      setCreatedAssistant((prev) => ({ ...prev, temperature: value }));
    }
  };

  const handleTopPChange = (event: Event, newValue: number | number[], id: string | null = null) => {
    const value = newValue as number;
    if (id) {
      setSelectedAssistant((prev) => ({ ...prev, top_p: value }));
    } else {
      setCreatedAssistant((prev) => ({ ...prev, top_p: value }));
    }
  };

  const handleTemperatureInputChange = (event: React.ChangeEvent<HTMLInputElement>, id: string | null = null) => {
    const value = parseFloat(event.target.value);
    if (!isNaN(value)) {
      if (id) {
        setSelectedAssistant((prev) => ({ ...prev, temperature: value }));
      } else {
        setCreatedAssistant((prev) => ({ ...prev, temperature: value }));
      }
    }
  };

  const handleTopPInputChange = (event: React.ChangeEvent<HTMLInputElement>, id: string | null = null) => {
    const value = parseFloat(event.target.value);
    if (!isNaN(value)) {
      if (id) {
        setSelectedAssistant((prev) => ({ ...prev, top_p: value }));
      } else {
        setCreatedAssistant((prev) => ({ ...prev, top_p: value }));
      }
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
    const newAssistant: CreateAssistantRequest = {
      name: createdAssistant.name || 'Untitled assistant',
      instructions: createdAssistant.instructions || '',
      model: createdAssistant.model || 'gpt-4o',
      temperature: createdAssistant.temperature || 1,
      top_p: createdAssistant.top_p || 1,
    };

    try {
      setLoading(true);
      const createdAssistantResponse = await postAssistant(settings.apiKey, newAssistant);
      setAssistants([...assistants, createdAssistantResponse]);
      setCreatedAssistant(true);
      setShowCreatePanel(false);
      setSelectedAssistant(emptyAssistantObject);
      await loadAssistants();
    } catch (error) {
      console.error('Error creating assistant:', error);
    } finally {
      setLoading(false);
    }
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

  const [settings] = useContext(SettingsContext);

  console.log(settings.apiKey);

async function loadAssistants() {
    setLoading(true);
    try {
      if (settings.apiKey) {
        console.log('openai token:', settings.apiKey);
        const response = await getAssistants(settings.apiKey);
        console.log('Full API response:', response);
        if (response.data) {
          setAssistants(response.data);
        } else {
          console.error('No data in API response');
        }
      } else {
        console.error('openai token is undefined');
      }
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  }

  const handleSelectAssistant = (id: string) => {
      const assistant = getAssistantById(assistants, id);
      if (assistant) {
        setSelectedAssistant(assistant); // Load assistant data into form
        setShowCreatePanel(true); // Show the creation panel
      } else {
        setShowAlert('Assistant not found');
      }
    };

const groupAssistantsByDate = (assistants) => {
  return assistants.reduce((acc, assistant) => {
    const date = moment(assistant.created_at * 1000).format('YYYY-MM-DD');
    if (!acc[date]) {
      acc[date] = [];
    }
    acc[date].push(assistant);
    return acc;
  }, {});
};

const groupedAssistants = groupAssistantsByDate(assistants);

const handleDeleteAssistant = async (id: string) => {
  try {
    setLoading(true);
    const deleteResponse = await deleteAssistant(settings.apiKey, id);
    const updatedAssistants = assistants.filter(assistant => assistant.id !== id);
    console.log('Updated assistants:', updatedAssistants);
    setAssistants(updatedAssistants);
    setSelectedAssistant(emptyAssistantObject);
    setShowAlert('Assistant deleted successfully');

  } catch (error) {
    console.error('Error deleting assistant:', error);
  } finally {
    setLoading(false);
  }
};

const handleUpdateAssistant = async () => {
  const updatedAssistant: ModifyAssistantRequest = {
    name: selectedAssistant.name || 'Untitled assistant',
    instructions: selectedAssistant.instructions || '',
    model: selectedAssistant.model || 'gpt-4o',
    temperature: selectedAssistant.temperature || 1,
    top_p: selectedAssistant.top_p || 1,
  };

  try {
    setLoading(true);
    const updatedAssistantResponse = await putAssistant(settings.apiKey, selectedAssistant.id, updatedAssistant);
    setAssistants(assistants.map(assistant =>
      assistant.id === selectedAssistant.id ? updatedAssistantResponse : assistant
    ));
    setShowAlert('Assistant updated successfully');
    setShowCreatePanel(false);
    setSelectedAssistant(emptyAssistantObject);
  } catch (error) {
    console.error('Error updating assistant:', error);
  } finally {
    setLoading(false);
  }
};

useEffect(() => {
    if (settings.apiKey) {
      loadAssistants();
    }
  }, [settings.apiKey]);

  return (
        <Box className={styles.container}>
              <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', gap: 0 }}>

                {/* Top Grid with Assistants title and Create button */}
                <Grid container justifyContent="space-between" alignItems="center" sx={{ p: 2, width: '100%', height: '100%' }}>
                    <Typography variant="h5">Assistants</Typography>
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={() => {
                          setShowCreatePanel(true);
                          setSelectedAssistant(emptyAssistantObject);
                        }}
                      sx={{ zIndex: 1 }}
                    >
                      + Create
                    </Button>
                </Grid>

                <Divider orientation="horizontal" flexItem sx={{ height: '100%' }} />

                <Box sx={{ display: 'flex', width: '100%', height: '100%' }}>
                    {/* Container of Assistants */}
                    <Grid container justifyContent="center" alignItems="flex-start" sx={{ p: 1, width: '100%', maxWidth: 350, height: '75vh', overflowY: 'auto' }}>
                      <div style={{ width: '100%', textAlign: 'center' }}>

                        {loading ? (
                          <Typography>Loading...</Typography>
                        ) : (
                          assistants.length === 0 && !createdAssistant ? (
                            <Typography>No assistants available.</Typography>
                          ) : (
                            <List>
                              {Object.keys(groupedAssistants).map((date) => (
                                <React.Fragment key={date}>
                                  <Box sx={{ width: '100%', textAlign: 'left', mt: 1 }}>
                                      <Typography variant="caption" sx={{ color: 'grey.600', fontWeight: 'bold' }}>
                                        {moment(date).format('YYYY-MM-DD')}
                                      </Typography>
                                  </Box>
                                  <Divider sx={{ mb: 2, mt:1}} />
                                  {groupedAssistants[date].map((assistant, index) => (
                                    <React.Fragment key={assistant.id}>
                                      <ListItem button sx={{ mt: index === 0 ? 0 : 2 }} onClick={() => handleSelectAssistant(assistant.id)}>
                                        <Grid container alignItems="center" spacing={1}>
                                          <Grid item sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', flexGrow: 1, minWidth: 0 }}>
                                            <Grid container direction="column" spacing={0.5}>
                                              <Typography className={styles.boldText} variant="body2">{assistant.name || "Untitled assistant"}</Typography>
                                              <Typography className={styles.smallText} variant="body2" color="textSecondary">{assistant.id}</Typography>
                                            </Grid>
                                          </Grid>

                                          <Grid item xs={4} sm={3} md={2} lg={2} xl={2} sx={{ minWidth: 0 }}>
                                              <Typography className={styles.smallText} variant="body2" sx={{ marginBottom: '8px', color: 'grey.600' }}>
                                                {moment(assistant.created_at * 1000).format('HH:mm')}
                                              </Typography>
                                          </Grid>
                                        </Grid>
                                      </ListItem>
                                      {index < groupedAssistants[date].length - 1 && <Divider sx={{ mb: 2, mt:1}} />}
                                    </React.Fragment>
                                  ))}
                                </React.Fragment>
                              ))}
                            </List>
                          )
                        )}
                      </div>
                    </Grid>

                 <Divider orientation="vertical" flexItem sx={{ height: '100vh' }} />

                  {/* Container of Create Assistant */}
                  <Grid container justifyContent="center" alignItems="center" sx={{ p: 2, width: 'calc(75% - 1px)', height: '75vh', overflowY: 'auto'}}>
                    <div style={{ width: '100%', textAlign: 'center', transition: 'width 0.3s', width: showCreatePanel ? '85%' : '30%'}}>

                      {/* Show message before clicking Create button */}
                      {!showCreatePanel && (
                        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '75vh' }}>
                          <Typography variant="subtitle2" sx={{ textAlign: 'center' }}>
                            Select an assistant to view details
                          </Typography>
                        </Box>
                      )}

                      {/* Creation panel */}
                      {showCreatePanel && (
                        <Paper elevation={3} sx={{ p: 2, width: '100%', height: '100%'}}>
                          <Box sx={{ paddingLeft: '20px', paddingRight: '20px' }}>
                            <Typography variant="h6" gutterBottom sx={{ textAlign: 'left' }}>
                              <Typography variant="subtitle3" sx={{ fontSize: 'smaller', fontWeight: 'normal', fontVariant: 'small-caps' }}>ASSISTANT</Typography>
                              <Typography variant="h5" sx={{ fontSize: 'large', fontWeight: 'bold' }}>
                                {selectedAssistant.id ? selectedAssistant.id : ''}
                              </Typography>
                            </Typography>

                            <TextField
                                fullWidth
                                label="Name"
                                value={selectedAssistant.id ? selectedAssistant.name : createdAssistant.name}
                                placeholder="Enter a user friendly name"
                                onChange={(e) =>
                                  selectedAssistant.id
                                    ? setSelectedAssistant({ ...selectedAssistant, name: e.target.value })
                                    : setCreatedAssistant({ ...createdAssistant, name: e.target.value })
                                }
                                margin="normal"
                            />
                            <TextField
                                fullWidth
                                label="Instructions"
                                placeholder="You are a helpful assistant"
                                multiline
                                rows={4}
                                margin="normal"
                                value={selectedAssistant.id ? selectedAssistant.instructions : createdAssistant.instructions}
                                onChange={(e) =>
                                  selectedAssistant.id
                                    ? setSelectedAssistant({ ...selectedAssistant, instructions: e.target.value })
                                    : setCreatedAssistant({ ...createdAssistant, instructions: e.target.value })
                                }
                            />
                            <TextField
                                fullWidth
                                select
                                label="Model"
                                value={selectedAssistant.id ? selectedAssistant.model : createdAssistant.model || 'gpt-4o'}
                                onChange={(e) =>
                                  selectedAssistant.id
                                    ? setSelectedAssistant({ ...selectedAssistant, model: e.target.value })
                                    : setCreatedAssistant({ ...createdAssistant, model: e.target.value })
                                }
                                margin="normal"
                                sx={{ display: 'block', mt: 3, mb: 3 }}
                                SelectProps={{
                                  native: true,
                                }}
                              >
                                {models.map((option) => (
                                  <option key={option.value} value={option.value}>
                                    {option.label}
                                  </option>
                                ))}
                            </TextField>
                            <Typography className={styles.boldText} align="left">Tools</Typography>
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

                              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1, mb: 3 }}>
                                <FormControlLabel
                                  control={<Switch checked={JsonObjectEnabled} onChange={handleJsonObjectChange} />}
                                  label="JSON object"
                                  sx={{ marginRight: '3px' }}
                                />
                                <Tooltip title="Constrains the model response to JSON format. You must specify the JSON format in your message. Only allowed when all tools on the Run are functions.">
                                  <img src={infoIcon} alt="Info" style={{ width: '15px', height: '15px' }}/>
                                </Tooltip>
                              </Box>
                            </div>

                            <div className={styles.sliders}>
                              <Typography gutterBottom>Temperature</Typography>
                              <TextField
                                value={selectedAssistant.id ? selectedAssistant.temperature : createdAssistant.temperature}
                                onChange={(e) => handleTemperatureInputChange(e, selectedAssistant && selectedAssistant.id ? selectedAssistant.id : null)}
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
                              value={selectedAssistant.id ? selectedAssistant.temperature : createdAssistant.temperature}
                              onChange={(e, newValue) => handleTemperatureChange(e, newValue, selectedAssistant && selectedAssistant.id ? selectedAssistant.id : null)}
                              step={0.01}
                              marks
                              min={0}
                              max={2}
                              valueLabelDisplay="auto"
                            />

                            <div className={styles.sliders}>
                              <Typography gutterBottom>Top P</Typography>
                              <TextField
                                value={selectedAssistant.id ? selectedAssistant.top_p : createdAssistant.top_p}
                                onChange={(e) => handleTopPInputChange(e, selectedAssistant && selectedAssistant.id ? selectedAssistant.id : null)}
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
                              value={selectedAssistant.id ? selectedAssistant.top_p : createdAssistant.top_p}
                              onChange={(e, newValue) => handleTopPChange(e, newValue, selectedAssistant && selectedAssistant.id ? selectedAssistant.id : null)}
                              step={0.01}
                              marks
                              min={0}
                              max={1}
                              valueLabelDisplay="auto"
                            />

                            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '2px' }}>
                              {selectedAssistant.id && (
                                <Button variant="contained" color="primary" onClick={() => handleDeleteAssistant(selectedAssistant.id)}>
                                  <img src={deleteIcon} alt="delete assistant" style={{ width: '20px', height: '20px' }}/>
                                </Button>
                              )}
                              <Button
                                  variant="contained"
                                  color="primary"
                                  onClick={selectedAssistant.id ? handleUpdateAssistant : handleCreateAssistant}
                                  style={{ marginLeft: 'auto' }}
                              >
                                  {selectedAssistant.id ? 'Update' : 'Save'}
                              </Button>
                            </div>
                          </Box>
                        </Paper>
                      )}
                    </div>
                  </Grid>
                 </Box>
                </Box>
          </Box>
      );
    }