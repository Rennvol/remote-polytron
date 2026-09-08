package com.polytron.remote
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView
class MainActivity: AppCompatActivity(){
  private var ir: ConsumerIrManager? = null
  private fun necPattern(addr:Int, cmd:Int): IntArray{
    val out = mutableListOf<Int>()
    out.add(9000); out.add(4500)
    val data = ((addr and 0xFF) or ((addr.inv() and 0xFF) shl 8) or ((cmd and 0xFF) shl 16) or ((cmd.inv() and 0xFF) shl 24)).toLong() and 0xFFFFFFFFL
    for(i in 0 until 32){
      out.add(560)
      out.add(if(((data shr i) and 1L)==1L) 1690 else 560)
    }
    out.add(560)
    return out.toIntArray()
  }
  // PLD 32T7511/S — primary 0x20DF, fallback 0x00FF — ponytail: hardcoded, add learn when differs
  private val codes = mapOf(
    "power" to listOf(necPattern(0x20DF, 0x10), necPattern(0x00FF, 0x10), necPattern(0x20DF, 0x0C)),
    "power_a" to listOf(necPattern(0x20DF, 0x10)),
    "power_b" to listOf(necPattern(0x00FF, 0x10)),
    "power_c" to listOf(necPattern(0x20DF, 0x0C)),
    "mute" to listOf(necPattern(0x20DF, 0x90)),
    "volp" to listOf(necPattern(0x20DF, 0x40)),
    "volm" to listOf(necPattern(0x20DF, 0xC0)),
    "chp" to listOf(necPattern(0x20DF, 0x00)),
    "chm" to listOf(necPattern(0x20DF, 0x80)),
    "source" to listOf(necPattern(0x20DF, 0xD0)),
    "menu" to listOf(necPattern(0x20DF, 0x58)),
    "exit" to listOf(necPattern(0x20DF, 0x5A)),
    "up" to listOf(necPattern(0x20DF, 0x02)),
    "down" to listOf(necPattern(0x20DF, 0x03)),
    "left" to listOf(necPattern(0x20DF, 0x06)),
    "right" to listOf(necPattern(0x20DF, 0x07)),
    "ok" to listOf(necPattern(0x20DF, 0x0A)),
    "0" to listOf(necPattern(0x20DF, 0x08)),
    "1" to listOf(necPattern(0x20DF, 0x88)),
    "2" to listOf(necPattern(0x20DF, 0x48)),
    "3" to listOf(necPattern(0x20DF, 0xC8)),
    "4" to listOf(necPattern(0x20DF, 0x28)),
    "5" to listOf(necPattern(0x20DF, 0xA8)),
    "6" to listOf(necPattern(0x20DF, 0x68)),
    "7" to listOf(necPattern(0x20DF, 0xE8)),
    "8" to listOf(necPattern(0x20DF, 0x18)),
    "9" to listOf(necPattern(0x20DF, 0x98))
  )
  @Volatile private var bruteStop=false
  private fun brutePower(){
    val mgr = ir ?: return
    if(!mgr.hasIrEmitter()) return
    val log = findViewById<TextView>(R.id.tWarn)
    bruteStop=false
    // ponytail: 2D brute common Polytron addrs x cmd, full 256*addr heavy ~30k tx
    val addrs = intArrayOf(0x20DF, 0x00FF, 0x807F, 0x40BF, 0x740B, 0x10EF, 0x48B7)
    Thread{
      outer@ for(a in addrs){
        for(c in 0..255){
          if(bruteStop) break@outer
          val pat = necPattern(a, c)
          try{ mgr.transmit(38000, pat) }catch(_:Exception){}
          val ah=Integer.toHexString(a).padStart(4,'0')
          val ch=Integer.toHexString(c).padStart(2,'0')
          runOnUiThread{ log.text = "brute "+ah+" 0x"+ch+" ("+c+") — Brute lagi=STOP / lihat TV"; log.visibility=android.view.View.VISIBLE }
          Thread.sleep(600)
        }
      }
      runOnUiThread{ Toast.makeText(this,"brute selesai — hex yg hidup apa?",Toast.LENGTH_LONG).show() }
    }.start()
  }
  private fun brutePowerQuick(){
    val mgr = ir ?: return
    if(!mgr.hasIrEmitter()) return
    val q = listOf(0x20DF to 0x10, 0x00FF to 0x10, 0x20DF to 0x0C, 0x807F to 0x10, 0x40BF to 0x10)
    Thread{ for((a,c) in q){ try{ mgr.transmit(38000, necPattern(a,c)) }catch(_:Exception){}; Thread.sleep(400) } }.start()
    Toast.makeText(this,"quick 5",Toast.LENGTH_SHORT).show()
  }
  private fun send(key:String){
    val mgr = ir
    if(mgr==null || !mgr.hasIrEmitter()){ Toast.makeText(this,"HP tidak ada IR blaster",Toast.LENGTH_SHORT).show(); return }
    val pats = codes[key] ?: return
    try{
      for(pat in pats){ mgr.transmit(38000, pat); if(pats.size>1) Thread.sleep(120) }
      Toast.makeText(this,if(key=="power") "power x${pats.size} (20DF/00FF)" else key, Toast.LENGTH_SHORT).show()
    }catch(e:Exception){ Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show() }
  }
  override fun onCreate(b:Bundle?){
    super.onCreate(b); setContentView(R.layout.activity_main)
    ir = getSystemService(CONSUMER_IR_SERVICE) as? ConsumerIrManager
    val warn = findViewById<TextView>(R.id.tWarn)
    val freq = ir?.carrierFrequencies
    if(ir==null || ir?.hasIrEmitter()==false){ warn.visibility=android.view.View.VISIBLE; warn.text="⚠ IR tidak terdeteksi" }
    else if(freq!=null){ warn.visibility=android.view.View.VISIBLE; warn.text="IR ready ${freq[0].minFrequency/1000}-${freq[0].maxFrequency/1000}kHz — arahkan 20cm ke sensor TV, lepas case jika tebal" }
    fun btn(id:Int, key:String){ findViewById<MaterialButton>(id).setOnClickListener{ send(key) } }
    btn(R.id.bPower,"power"); findViewById<MaterialButton>(R.id.bBrute).setOnClickListener{ if(bruteStop) bruteStop=false; else if(findViewById<TextView>(R.id.tWarn).text.toString().startsWith("brute")){ bruteStop=true; Toast.makeText(this,"stop",Toast.LENGTH_SHORT).show() } else brutePower() }; btn(R.id.bMute,"mute"); btn(R.id.bVolP,"volp"); btn(R.id.bVolM,"volm"); btn(R.id.bChP,"chp"); btn(R.id.bChM,"chm")
    btn(R.id.bSource,"source"); btn(R.id.bMenu,"menu"); btn(R.id.bExit,"exit")
    btn(R.id.b0,"0"); btn(R.id.b1,"1"); btn(R.id.b2,"2"); btn(R.id.b3,"3"); btn(R.id.b4,"4"); btn(R.id.b5,"5"); btn(R.id.b6,"6"); btn(R.id.b7,"7"); btn(R.id.b8,"8"); btn(R.id.b9,"9")
    btn(R.id.bUp,"up"); btn(R.id.bDown,"down"); btn(R.id.bLeft,"left"); btn(R.id.bRight,"right"); btn(R.id.bOk,"ok")
    // long-press power = brute 3 varian satu2
    findViewById<MaterialButton>(R.id.bPower).setOnLongClickListener{ send("power_a"); Toast.makeText(this,"power A 20DF 0x10",Toast.LENGTH_SHORT).show(); true }
  }
}
