// Pembungkus Firebase untuk aplikasi TK An Najah -> window.FB
import { initializeApp } from 'firebase/app';
import { getAuth, onAuthStateChanged, signInWithEmailAndPassword, createUserWithEmailAndPassword, signOut, sendPasswordResetEmail } from 'firebase/auth';
import { initializeFirestore, persistentLocalCache, persistentMultipleTabManager, memoryLocalCache, doc, getDoc, setDoc, onSnapshot, collection, getDocs, deleteField, arrayUnion, arrayRemove } from 'firebase/firestore';

const DEL = { __fbdel: true };
let app, auth, db;

function conv(v) {
  if (v === DEL) return deleteField();
  if (v && typeof v === 'object' && v.__fbArr) return v.op === 'union' ? arrayUnion(...v.items) : arrayRemove(...v.items);
  if (Array.isArray(v)) return v.map(conv);
  if (v && typeof v === 'object') { const o = {}; for (const k of Object.keys(v)) o[k] = conv(v[k]); return o; }
  return v;
}

window.FB = {
  DEL,
  arrayUnion: (...items) => ({ __fbArr: true, op: 'union', items }),
  arrayRemove: (...items) => ({ __fbArr: true, op: 'remove', items }),
  init(config) {
    app = initializeApp(config);
    auth = getAuth(app);
    try {
      db = initializeFirestore(app, { localCache: persistentLocalCache({ tabManager: persistentMultipleTabManager() }) });
    } catch (e) {
      db = initializeFirestore(app, { localCache: memoryLocalCache() });
    }
  },
  auth: {
    onChange(cb) { return onAuthStateChanged(auth, u => cb(u ? { uid: u.uid, email: u.email } : null)); },
    async signIn(email, pw) { const c = await signInWithEmailAndPassword(auth, email, pw); return { uid: c.user.uid, email: c.user.email }; },
    async signUp(email, pw) { const c = await createUserWithEmailAndPassword(auth, email, pw); return { uid: c.user.uid, email: c.user.email }; },
    signOut() { return signOut(auth); },
    reset(email) { return sendPasswordResetEmail(auth, email); },
  },
  db: {
    async get(path) { const s = await getDoc(doc(db, path)); return s.exists() ? s.data() : null; },
    set(path, data, merge) { return setDoc(doc(db, path), conv(data), { merge: !!merge }); },
    listen(path, cb, err) {
      return onSnapshot(doc(db, path), { includeMetadataChanges: false },
        s => cb(s.exists() ? s.data() : null, { fromCache: s.metadata.fromCache, pending: s.metadata.hasPendingWrites }),
        e => err && err(e));
    },
    async list(path) { const q = await getDocs(collection(db, path)); return q.docs.map(d => ({ id: d.id, data: d.data() })); },
  },
};
