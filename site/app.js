const repo='Lanzkila/KirinYT';const api='https://api.github.com/repos/'+repo;
const fmt=n=>n>=1e6?(n/1e6).toFixed(1)+'M':n>=1e3?(n/1e3).toFixed(1)+'K':String(n);
async function load(){
 try{
  const [r,rels]=await Promise.all([fetch(api),fetch(api+'/releases?per_page=30')]);
  const info=await r.json(), releases=await rels.json();
  document.getElementById('stars').textContent=fmt(info.stargazers_count||0);
  document.getElementById('forks').textContent=fmt(info.forks_count||0);
  let total=0; if(Array.isArray(releases)) releases.forEach(x=>(x.assets||[]).forEach(a=>total+=a.download_count||0));
  document.getElementById('downloads').textContent=fmt(total);
  const latest=Array.isArray(releases)?releases.find(x=>!x.draft):null;
  if(latest){
   document.getElementById('releaseName').textContent=latest.name||latest.tag_name;
   document.getElementById('releaseDate').textContent='Published '+new Date(latest.published_at).toLocaleDateString(undefined,{year:'numeric',month:'long',day:'numeric'});
   document.getElementById('releaseLink').href=latest.html_url;
   document.getElementById('downloadBtn').href=latest.html_url;
  }
 }catch(e){}
}load();