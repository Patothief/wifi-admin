import React from 'react';
import { createRoot } from 'react-dom/client';
import {
  AlertTriangle,
  CheckCircle2,
  KeyRound,
  Loader2,
  RefreshCw,
  Router,
  Save,
  Search,
  Server,
  ShieldCheck,
  Wifi,
} from 'lucide-react';
import './styles.css';

const WIFI_BANDS = [
  { value: 'BAND_2_4_GHZ', label: '2.4 GHz' },
  { value: 'BAND_5_GHZ', label: '5 GHz' },
];

const ENCRYPTION_TYPES = [
  'OPEN',
  'WEP',
  'WPA_PSK',
  'WPA2_PSK',
  'WPA3_SAE',
  'WPA2_ENTERPRISE',
];

const DEFAULT_CONFIGURATION = {
  cpeId: 'CPE_001',
  wifiBand: 'BAND_2_4_GHZ',
  ssid: '',
  encryptionType: 'OPEN',
  password: '',
};

const defaultApiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api';

function App() {
  const [apiBaseUrl, setApiBaseUrl] = usePersistentState('wifi-admin.apiBaseUrl', defaultApiBaseUrl);
  const [apiKey, setApiKey] = usePersistentState('wifi-admin.apiKey', '');
  const [lookupCpeId, setLookupCpeId] = usePersistentState('wifi-admin.lookupCpeId', 'CPE_001');
  const [configuration, setConfiguration] = React.useState(DEFAULT_CONFIGURATION);
  const [state, setState] = React.useState({
    busy: false,
    type: 'idle',
    message: 'Ready',
    requestId: '',
    statusCode: '',
    updatedAt: '',
  });

  const updateConfiguration = (field, value) => {
    setConfiguration((current) => ({ ...current, [field]: value }));
  };

  const fetchConfiguration = async () => {
    const cpeId = lookupCpeId.trim();
    if (!cpeId) {
      setState(statusState('error', 'CPE ID is required'));
      return;
    }

    await runRequest(async (requestId) => {
      const response = await fetch(`${trimTrailingSlash(apiBaseUrl)}/wifi-parameter/${encodeURIComponent(cpeId)}`, {
        headers: requestHeaders(apiKey, requestId),
      });
      const body = await readJson(response);

      if (!response.ok) {
        throw responseError(response, body, requestId);
      }

      setConfiguration(normalizeConfiguration(body));
      setLookupCpeId(body.cpeId || cpeId);
      return {
        message: `Loaded ${body.cpeId || cpeId}`,
        requestId: response.headers.get('X-Request-Id') || requestId,
        statusCode: response.status,
      };
    });
  };

  const saveConfiguration = async () => {
    const payload = normalizeConfiguration(configuration);
    const validationError = validateConfiguration(payload);
    if (validationError) {
      setState(statusState('error', validationError));
      return;
    }

    await runRequest(async (requestId) => {
      const response = await fetch(`${trimTrailingSlash(apiBaseUrl)}/wifi-parameter`, {
        method: 'PUT',
        headers: {
          ...requestHeaders(apiKey, requestId),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });
      const body = await readJson(response);

      if (!response.ok) {
        throw responseError(response, body, requestId);
      }

      setConfiguration(normalizeConfiguration(body));
      setLookupCpeId(body.cpeId || payload.cpeId);
      return {
        message: `Saved ${body.cpeId || payload.cpeId}`,
        requestId: response.headers.get('X-Request-Id') || requestId,
        statusCode: response.status,
      };
    });
  };

  const runRequest = async (request) => {
    const requestId = `ui-${crypto.randomUUID()}`;
    setState((current) => ({ ...current, busy: true, type: 'loading', message: 'Working', requestId }));

    try {
      const result = await request(requestId);
      setState(statusState('success', result.message, result.requestId, result.statusCode));
    } catch (error) {
      setState(statusState('error', error.message, error.requestId || requestId, error.statusCode || ''));
    }
  };

  const secured = apiKey.trim().length > 0;
  const passwordDisabled = configuration.encryptionType === 'OPEN';

  return (
    <main className="appShell">
      <header className="topBar">
        <div className="brandBlock">
          <div className="brandMark" aria-hidden="true">
            <Wifi size={24} />
          </div>
          <div>
            <h1>WiFi Admin</h1>
            <p>REST console for CPE wireless configuration</p>
          </div>
        </div>
        <div className={`statusPill ${state.type}`}>
          {state.busy ? <Loader2 className="spin" size={16} /> : state.type === 'error' ? <AlertTriangle size={16} /> : <CheckCircle2 size={16} />}
          <span>{state.message}</span>
        </div>
      </header>

      <section className="networkStrip" aria-label="Connection overview">
        <div className="node">
          <Router size={20} />
          <span>CPE</span>
        </div>
        <div className="linkLine" />
        <div className="node active">
          <Wifi size={20} />
          <span>{configuration.ssid || 'SSID'}</span>
        </div>
        <div className="linkLine" />
        <div className="node">
          <Server size={20} />
          <span>{apiBaseUrl}</span>
        </div>
      </section>

      <section className="workspace">
        <aside className="panel controlPanel">
          <div className="panelHeader">
            <h2>Connection</h2>
            <ShieldCheck size={18} />
          </div>

          <label className="field">
            <span>API base URL</span>
            <input value={apiBaseUrl} onChange={(event) => setApiBaseUrl(event.target.value)} />
          </label>

          <label className="field">
            <span>API key</span>
            <div className="iconInput">
              <KeyRound size={16} />
              <input
                value={apiKey}
                type="password"
                onChange={(event) => setApiKey(event.target.value)}
                placeholder="Optional"
              />
            </div>
          </label>

          <label className="field">
            <span>CPE ID</span>
            <input value={lookupCpeId} onChange={(event) => setLookupCpeId(event.target.value)} />
          </label>

          <button className="primaryAction" type="button" onClick={fetchConfiguration} disabled={state.busy}>
            {state.busy ? <Loader2 className="spin" size={18} /> : <Search size={18} />}
            <span>Fetch</span>
          </button>

          <div className="metaRows">
            <div>
              <span>Security</span>
              <strong>{secured ? 'Header set' : 'Disabled'}</strong>
            </div>
            <div>
              <span>Status</span>
              <strong>{state.statusCode || '-'}</strong>
            </div>
          </div>
        </aside>

        <section className="panel editorPanel">
          <div className="panelHeader">
            <h2>WiFi configuration</h2>
            <RefreshCw size={18} />
          </div>

          <div className="formGrid">
            <label className="field">
              <span>CPE ID</span>
              <input value={configuration.cpeId} onChange={(event) => updateConfiguration('cpeId', event.target.value)} />
            </label>

            <label className="field">
              <span>SSID</span>
              <input value={configuration.ssid} onChange={(event) => updateConfiguration('ssid', event.target.value)} />
            </label>

            <fieldset className="segmentedField">
              <legend>Band</legend>
              <div className="segmented">
                {WIFI_BANDS.map((band) => (
                  <button
                    key={band.value}
                    type="button"
                    className={configuration.wifiBand === band.value ? 'selected' : ''}
                    onClick={() => updateConfiguration('wifiBand', band.value)}
                  >
                    {band.label}
                  </button>
                ))}
              </div>
            </fieldset>

            <label className="field">
              <span>Encryption</span>
              <select
                value={configuration.encryptionType}
                onChange={(event) => updateConfiguration('encryptionType', event.target.value)}
              >
                {ENCRYPTION_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </label>

            <label className="field wide">
              <span>Password</span>
              <input
                value={configuration.password || ''}
                type="password"
                disabled={passwordDisabled}
                onChange={(event) => updateConfiguration('password', event.target.value)}
              />
            </label>
          </div>

          <div className="actionRow">
            <button className="secondaryAction" type="button" onClick={fetchConfiguration} disabled={state.busy}>
              <RefreshCw size={18} />
              <span>Reload</span>
            </button>
            <button className="primaryAction" type="button" onClick={saveConfiguration} disabled={state.busy}>
              {state.busy ? <Loader2 className="spin" size={18} /> : <Save size={18} />}
              <span>Save</span>
            </button>
          </div>
        </section>

        <aside className="panel responsePanel">
          <div className="panelHeader">
            <h2>Request</h2>
            <Server size={18} />
          </div>
          <dl className="requestDetails">
            <div>
              <dt>Request ID</dt>
              <dd>{state.requestId || '-'}</dd>
            </div>
            <div>
              <dt>Last update</dt>
              <dd>{state.updatedAt || '-'}</dd>
            </div>
            <div>
              <dt>Payload</dt>
              <dd>
                <pre>{JSON.stringify(normalizeConfiguration(configuration), null, 2)}</pre>
              </dd>
            </div>
          </dl>
        </aside>
      </section>
    </main>
  );
}

function usePersistentState(key, initialValue) {
  const [value, setValue] = React.useState(() => localStorage.getItem(key) || initialValue);

  React.useEffect(() => {
    localStorage.setItem(key, value);
  }, [key, value]);

  return [value, setValue];
}

function requestHeaders(apiKey, requestId) {
  const headers = {
    Accept: 'application/json',
    'X-Request-Id': requestId,
  };

  if (apiKey.trim()) {
    headers['X-API-Key'] = apiKey.trim();
  }

  return headers;
}

async function readJson(response) {
  const text = await response.text();
  if (!text) {
    return {};
  }

  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}

function responseError(response, body, requestId) {
  const message = body.message || `Request failed with status ${response.status}`;
  const error = new Error(message);
  error.statusCode = response.status;
  error.requestId = response.headers.get('X-Request-Id') || requestId;
  return error;
}

function normalizeConfiguration(configuration) {
  const encryptionType = configuration.encryptionType || 'OPEN';
  return {
    cpeId: configuration.cpeId || '',
    wifiBand: configuration.wifiBand || 'BAND_2_4_GHZ',
    ssid: configuration.ssid || '',
    encryptionType,
    password: encryptionType === 'OPEN' ? null : configuration.password || '',
  };
}

function validateConfiguration(configuration) {
  if (!configuration.cpeId.trim()) {
    return 'CPE ID is required';
  }
  if (!configuration.ssid.trim()) {
    return 'SSID is required';
  }
  if (configuration.encryptionType !== 'OPEN' && !configuration.password) {
    return `Password is required for ${configuration.encryptionType}`;
  }
  return '';
}

function statusState(type, message, requestId = '', statusCode = '') {
  return {
    busy: false,
    type,
    message,
    requestId,
    statusCode,
    updatedAt: new Date().toLocaleTimeString(),
  };
}

function trimTrailingSlash(value) {
  return value.replace(/\/$/, '');
}

createRoot(document.getElementById('root')).render(<App />);
