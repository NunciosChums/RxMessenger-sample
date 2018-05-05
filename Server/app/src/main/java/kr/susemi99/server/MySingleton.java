package kr.susemi99.server;

public class MySingleton {
  private static final MySingleton ourInstance = new MySingleton();

  public static MySingleton getInstance() {
    return ourInstance;
  }

  private int count = 0;

  private MySingleton() { }

  public int getCount() {
    return ++count;
  }
}
