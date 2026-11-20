// Firebase tiruan (hanya untuk pengujian) — data bersama via localStorage agar beberapa tab = beberapa guru
(function(){
const DEL={__fbdel:true};
const K='__mockfb';
const load=()=>{try{return JSON.parse(localStorage.getItem(K))||{docs:{},users:{}}}catch(e){return {docs:{},users:{}}}};
const saveAll=s=>localStorage.setItem(K,JSON.stringify(s));
const listeners={};let authCb=null;let cur=JSON.parse(sessionStorage.getItem('__mockcur')||'null');
function fire(path){const d=load().docs[path];(listeners[path]||[]).forEach(cb=>setTimeout(()=>cb(d?JSON.parse(JSON.stringify(d)):null,{fromCache:false}),5))}
window.addEventListener('storage',e=>{if(e.key===K)Object.keys(listeners).forEach(fire)});
function merge(t,s){for(const k of Object.keys(s)){const v=s[k];
  if(v===DEL||(v&&v.__fbdel)){delete t[k];continue}
  if(v&&typeof v==='object'&&v.__fbArr){const a=Array.isArray(t[k])?t[k]:[];t[k]=v.op==='union'?[...a,...v.items.filter(i=>!a.includes(i))]:a.filter(i=>!v.items.includes(i));continue}
  if(v&&typeof v==='object'&&!Array.isArray(v)){if(!t[k]||typeof t[k]!=='object'||Array.isArray(t[k]))t[k]={};merge(t[k],v);continue}
  t[k]=v}}
window.__mockWrites=0;
window.FB={DEL,arrayUnion:(...items)=>({__fbArr:true,op:'union',items}),arrayRemove:(...items)=>({__fbArr:true,op:'remove',items}),
 init(){},
 auth:{onChange(cb){authCb=cb;setTimeout(()=>cb(cur),10);return()=>{}},
  async signIn(e,p){const u=load().users[e];if(!u||u.p!==p)throw {code:'auth/invalid-credential'};cur={uid:u.uid,email:e};sessionStorage.setItem('__mockcur',JSON.stringify(cur));authCb&&authCb(cur);return cur},
  async signUp(e,p){const s=load();if(s.users[e])throw {code:'auth/email-already-in-use'};if(p.length<6)throw {code:'auth/weak-password'};const uid='u'+Math.random().toString(36).slice(2,8);s.users[e]={uid,p};saveAll(s);cur={uid,email:e};sessionStorage.setItem('__mockcur',JSON.stringify(cur));authCb&&authCb(cur);return cur},
  async signOut(){cur=null;sessionStorage.removeItem('__mockcur');authCb&&authCb(null)},
  async reset(){}},
 db:{async get(p){const d=load().docs[p];return d?JSON.parse(JSON.stringify(d)):null},
  async set(p,data,m){window.__mockWrites++;const s=load();if(m){s.docs[p]=s.docs[p]||{};merge(s.docs[p],JSON.parse(JSON.stringify(data,(k,v)=>v===DEL?{__fbdel:true}:v)))}else{const t={};merge(t,JSON.parse(JSON.stringify(data)));s.docs[p]=t}saveAll(s);fire(p)},
  listen(p,cb){(listeners[p]=listeners[p]||[]).push(cb);setTimeout(()=>{const d=load().docs[p];cb(d?JSON.parse(JSON.stringify(d)):null,{fromCache:false})},5);return()=>{listeners[p]=(listeners[p]||[]).filter(x=>x!==cb)}},
  async list(p){const s=load();const depth=p.split('/').length+1;return Object.keys(s.docs).filter(k=>k.startsWith(p+'/')&&k.split('/').length===depth).map(k=>({id:k.split('/').pop(),data:s.docs[k]}))}}};
})();
