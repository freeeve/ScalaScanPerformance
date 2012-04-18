package scala.scanperformance
import scala.collection.immutable.Seq;
import scala.collection.mutable.ArraySeq;

object ScanPerformance {
  def main(args:Array[String]):Unit = {
    // initialize the pool
    val pool = {
      var p = new ArraySeq[PoolInstance](31);
      var rand = scala.util.Random;
      for(k <- 0 until 31) {
        p(k) = new PoolInstance(rand.nextInt(3) == 1); // 33% chance active
      }
      p.toSeq;
    }
    
    // let's see how fast it is with just a naive scan
    var start = System.currentTimeMillis();
    var count = 0l;
    for(i <- 1 to 1000000) {
      for(k <- 0 until 31) {
        if(pool(k).active == true) {
          count += 1;
        }
      }
    }
    var end = System.currentTimeMillis();
    printf("naive scanned a million times in: %d ms (sanity check: %d)\n",end-start, count);
    
    // let's try a filter with foreach
    count = 0;
    start = System.currentTimeMillis();
    for(i <- 1 to 1000000) {
      pool.filter( s => s.active == true).foreach(inst => {
        count += 1;
      });
    }
    end = System.currentTimeMillis();
    printf("filter/foreach scanned a million times in: %d ms (sanity check: %d)\n",end-start, count);
    
    // let's try a filter with map
    count = 0;
    start = System.currentTimeMillis();
    for(i <- 1 to 1000000) {
      pool.map(inst => {
        if(inst.active == true) {
          count += 1;
        }
      });
    }
    end = System.currentTimeMillis();
    printf("map scanned a million times in: %d ms (sanity check: %d)\n",end-start, count);
    
    // let's try a bitmap scan
    var bitMap:Int = {
      var bmp = 0;
      for(i <- 0 until 31) {
        if(pool(i).active==true) {
          bmp |= (1 << i);
        }
      }
      bmp;
    }
    count = 0;
    start = System.currentTimeMillis();
    for(i <- 1 to 1000000) {
      var bitMapCopy = bitMap;
      var s = 0;
      while(bitMapCopy > 0) {
        s = findFirstBitSet(bitMapCopy);
        bitMapCopy ^= (1 << s);
        count+=1;
      }
    }
    end = System.currentTimeMillis();
    printf("bitmap scanned a million times in: %d ms (sanity check: %d)\n",end-start, count);
  }
  
  val mod37BitPosition = Seq[Int](
      32, 0, 1, 26, 2, 23, 27, 0, 3, 16, 24, 30, 28, 11, 0, 13, 4,
      7, 17, 0, 25, 22, 31, 15, 29, 10, 12, 6, 0, 21, 14, 9, 5,
      20, 8, 19, 18
    );
  
  def findFirstBitSet(i:Int):Int = {
    mod37BitPosition((-i & i) % 37);
  }
}

class PoolInstance(var active:Boolean = false) {}

