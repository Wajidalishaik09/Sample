const {chromium}=require('playwright'); const path=require('path'); const fs=require('fs');
const DIR=__dirname, FPS=24, DUR=30;
(async()=>{ const b=await chromium.launch(); const p=await b.newPage({viewport:{width:1080,height:1920}});
 await p.goto('file://'+path.join(DIR,'ad.html')); await p.waitForTimeout(2500); await p.evaluate(()=>document.fonts.ready);
 fs.rmSync(path.join(DIR,'frames'),{recursive:true,force:true}); fs.mkdirSync(path.join(DIR,'frames'));
 // preload all images
 await p.evaluate(async()=>{ const L=['01-splash','02-why','03-header','04-masjid','05-mission','06-reward','07-ameen','08-qibla','09-badges','10-sharecard'];
   await Promise.all(L.map(n=>new Promise(r=>{const i=new Image(); i.onload=i.onerror=r; i.src='shots/'+n+'.png';}))); });
 for(let i=0;i<FPS*DUR;i++){ await p.evaluate(t=>window.render(t), i/FPS); if(i%1===0) await p.waitForTimeout(i===0?300:0);
   await p.screenshot({path:path.join(DIR,'frames',String(i).padStart(4,'0')+'.jpg'),type:'jpeg',quality:90}); }
 await b.close(); })();
