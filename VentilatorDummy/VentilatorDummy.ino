/*
  AnalogReadSerial

  Reads an analog input on pin 0, prints the result to the Serial Monitor.
  Graphical representation is available using Serial Plotter (Tools > Serial Plotter menu).
  Attach the center pin of a potentiometer to pin A0, and the outside pins to +5V and ground.

  This example code is in the public domain.

  http://www.arduino.cc/en/Tutorial/AnalogReadSerial
*/

// the setup routine runs once when you press reset:
void setup() {
  // initialize serial communication at 9600 bits per second:
  Serial.begin(115200);
}

// the loop routine runs over and over again forever:
void loop() {
  // print out the value you read:
  
  int pressure = 300;
  byte pressureBytes[2];
  pressureBytes[0]=(pressure>>8);
  pressureBytes[1]=pressure;
  
  int oxy = 350;
  byte oxyBytes[2];
  oxyBytes[0]=(oxy>>8);
  oxyBytes[1]=oxy;
  
  int flow = 260;
  byte flowBytes[2];
  flowBytes[0]=(flow>>8);
  flowBytes[1]=flow;
  
  int bpm = 400;
  byte bpmBytes[2];
  bpmBytes[0]=(bpm>>8);
  bpmBytes[1]=bpm;
  
  int vol = 310;
  byte volBytes[2];
  volBytes[0]=(vol>>8);
  volBytes[1]=vol;
  
  Serial.print("its");
  Serial.write(pressureBytes[1]);
  Serial.write(pressureBytes[0]);
  Serial.write(oxyBytes[1]);
  Serial.write(oxyBytes[0]);
  Serial.write(flowBytes[1]);
  Serial.write(flowBytes[0]);
  Serial.write(bpmBytes[1]);
  Serial.write(bpmBytes[0]);
  Serial.write(volBytes[1]);
  Serial.write(volBytes[0]);
  Serial.print("#");
  Serial.print("\n");
  delay(1000);        // delay in between reads for stability
}
