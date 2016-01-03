import java.io.*;
import java.util.*;

public class Onepass
{

	public static void main(String[] args) throws FileNotFoundException,
			IOException
	{

		HashMap<String, String> optab = new HashMap<>();
		File foptab = new File("OPTAB.DAT");
		File finput = new File("Source.DAT");
		FileInputStream fipoptab = new FileInputStream(foptab);
		FileInputStream fipinput = new FileInputStream(finput);
		int size;
		int baseRegister, PC;
		String controlSection = "";
		int multiOperandFlag = 0;
		String[] tempoperand;
		ArrayList<String> extdef = new ArrayList<>();
		ArrayList<String> extref = new ArrayList<>();
		String temp1 = "", temp2 = "";
		int flag1 = 0;
		char temp;
		size = fipoptab.available();
		for (int i = 0; i < size; ++i)
		{
			temp = (char) fipoptab.read();
			if (flag1 == 0 && temp != ' ')
				temp1 += temp;

			else if (temp != '\n' && temp != ' ')
				temp2 += temp;

			if (temp == ' ')
				++flag1;

			if (temp == '\n')
			{
				optab.put(temp1, temp2);
				temp1 = "";
				temp2 = "";
				flag1 = 0;
			}
		}

		// Taking input from file and breaking into label, mnemonic and __ :
		ArrayList<ArrayList<String>> input = new ArrayList<>(); 

		String strtemp = "";
		size = fipinput.available();
		for (int j = 0; j < size; ++j)
		{
			temp = (char) fipinput.read();
			if (temp != '\n')
			{
				strtemp += temp;
			}
			else
			{
				List<String> split = Arrays.asList(strtemp.split(" "));
				strtemp = "";
				ArrayList<String> _split = new ArrayList<>();
				for (String i : split)
				{
					_split.add(i);
				}
				_split.add(0, "0");
				input.add(_split);
			}
		}
		String LOCCTR = "0";
		if (!(input.get(0).get(3).isEmpty()))
		{
			LOCCTR = input.get(0).get(3);
		}

		// ASSEMBLER DIRECTIVES :

		ArrayList<String> assdir = new ArrayList<>();
		assdir.add("START");
		assdir.add("END");
		assdir.add("EXTDEF");
		assdir.add("EXTREF");
		assdir.add("EQU");
		assdir.add("ORG");

		HashMap<String, ArrayList<String>> estab = new HashMap<>();
		ArrayList<String> tempestab = new ArrayList<>();
		ArrayList<String> tempArrayList = new ArrayList<>();

		ArrayList<ArrayList<String>> tempop = new ArrayList<>();

		// SYMTAB OPERATIONS :
		HashMap<String, LinkedList<String>> symtab = new HashMap<>();

		for (ArrayList<String> i : input)
		{
			i.set(0, LOCCTR);
			String label = i.get(1);
			String mnemonic = i.get(2);
			String operand = " ";
			if (!mnemonic.equals("EXTDEF") && !mnemonic.equals("EXTREF"))
			{
				if (!mnemonic.equals("RSUB"))
					operand = i.get(3);
			}
			else
			{
				i.add(null);
				i.add(null);
			}
			String opcode, opaddress;
			if (!(label.equals(".")))
			{

				/********* Label field handling *******************/

				if (!label.equals(""))
				{
					if (symtab.containsKey(label))
					{
						LinkedList<String> SymbolList = symtab.get(label);
						List<String> sublist;
						sublist = SymbolList.subList(1, SymbolList.size());
						if (sublist.isEmpty())
						{
							sublist.add(SymbolList.get(1));
						}

						if (SymbolList.getFirst() == null)
						{
							SymbolList.set(0, LOCCTR);
							for (String j : sublist)
							{
								if (j != null)
								{
									ListIterator<ArrayList<String>> _tuple_ = tempop
											.listIterator();
									while (_tuple_.hasNext())
									{
										String tupleval = _tuple_.next().get(0);
										if (tupleval.equals(j))
										{
											ArrayList<String> temp_tuple = _tuple_
													.previous();
											temp_tuple.set(4, temp_tuple.get(4)
													+ LOCCTR);
											_tuple_.set(temp_tuple);
											_tuple_.next();
										}
									}
								}
							} 
							String string = SymbolList.getFirst();
							SymbolList.clear();
							SymbolList.add(string);
							symtab.remove(label);
							symtab.put(label, SymbolList);
						}

					}

					else
					{ 
						LinkedList<String> tempValue = new LinkedList<>();
						tempValue.add(LOCCTR);
						symtab.put(label, tempValue);
					}
				}

				/********* Mnemonic and Operand Field handling ***************/

				i.set(0, LOCCTR);
				if (optab.containsKey(mnemonic)
						|| optab.containsKey(mnemonic.substring(1)))
				{
					opcode = optab.get(mnemonic);
					if (mnemonic.startsWith("+"))
						opcode = optab.get(mnemonic.substring(1));
					// Support for SICXE addressing mode :

					if (operand.startsWith("@"))
					{ // Indirect Addressing
						int _opcode = Integer.parseInt(opcode, 16);
						_opcode += 2;
						opcode = Integer.toHexString(_opcode);
					}
					else if (operand.startsWith("#"))
					{ // Immediate Addressing
						int _opcode = Integer.parseInt(opcode, 16);
						_opcode += 1;
						opcode = Integer.toHexString(_opcode);
						String tempOperand = operand.substring(1);
						opaddress = tempOperand;
						for (int j = opaddress.length(); j < 4; ++j)
							opaddress = "0" + opaddress;
						opcode += opaddress;
					}

					else
					{
						if (!(operand.startsWith("=") || mnemonic
								.startsWith("+")))
						{ 
							int _opcode = Integer.parseInt(opcode, 16);
							_opcode += 3;
							for (int j = opcode.length(); j < 2; ++j)
								opcode = "0" + opcode;
						}

					}
					if (mnemonic.startsWith("+"))
						opcode += "10";
					if (!mnemonic.equals("RSUB"))
						i.add(4, opcode);
					else
					{
						i.add(null);
						i.add(opcode);
					}

					/******** Check for multiple operands *********/

					char operandOperator = ' ';
					if (operand.contains("+"))
					{
						operandOperator = '+';
					}
					if (operand.contains("-"))
					{
						operandOperator = '-';
					}
					if (operand.contains("*"))
					{
						operandOperator = '*';
					}
					if (operand.contains("/"))
					{
						operandOperator = '/';
					}

					tempoperand = operand.split(" ");
					if (operand.contains(","))
						tempoperand = operand.split(",");
					if (operand.contains("*"))
						tempoperand = operand.split("\\+");
					if (operand.contains("-"))
						tempoperand = operand.split("\\-");
					if (operand.contains("*"))
						tempoperand = operand.split("\\*");
					if (operand.contains("/"))
						tempoperand = operand.split("\\/");

					/************* Check if OPERAND is in SYMTAB ************/

					for (String _operand : tempoperand)
					{
						if (!_operand.startsWith("#"))
						{
							if (symtab.containsKey(_operand))
							{

								/******* Check if symtab contains _operand ********/

								if (symtab.get(_operand).getFirst() != null)
								{
									opaddress = symtab.get(_operand).toString();
									opaddress = opaddress.replace("[", "");
									opaddress = opaddress.replaceAll("]", "");
									String topaddress = opcode + opaddress;
									i.set(4, topaddress);
									operand += opaddress;
								}
								else
								{
									symtab.get(_operand).add(LOCCTR);
								}
							}

							else
							/** _operand not in SYMTAB **/
							{
								LinkedList<String> templist = new LinkedList<>();
								templist.add(null);
								templist.add(LOCCTR);
								symtab.put(_operand, templist);

							}
							if (extref.contains(_operand)
									|| extdef.contains(_operand))
							{
								opaddress = "0000";
								String topaddress = opcode + opaddress;
								i.set(4, topaddress);
							}
						}
					}
					
					if (mnemonic.equals("TIXR") || mnemonic.equals("COMPR"))
					{
						int _intLOCCTR = (Integer.parseInt(LOCCTR, 16)) + 2;
						LOCCTR = Integer.toHexString(_intLOCCTR);
					}
					else if (mnemonic.startsWith("+"))
					{
						int _intLOCCTR = (Integer.parseInt(LOCCTR, 16)) + 4;
						LOCCTR = Integer.toHexString(_intLOCCTR);
					}
					else
					{
						int intLOCCTR = (Integer.parseInt(LOCCTR, 16)) + 3;
						LOCCTR = Integer.toHexString(intLOCCTR);
					}

				}

				/** OPTAB does not contain mnemonic i.e. ASSEMBLER DIRECTIVES **/

				else if (mnemonic.equals("WORD"))
				{
					int intLOCCTR = (Integer.parseInt(LOCCTR, 16)) + 3;
					LOCCTR = Integer.toHexString(intLOCCTR);
					i.add(null);
				}
				else if (mnemonic.equals("RESW"))
				{
					int intLOCCTR = (Integer.parseInt(LOCCTR, 16)) + 3
							* Integer.parseInt(operand);
					LOCCTR = Integer.toHexString(intLOCCTR);
					i.add(null);
				}
				else if (mnemonic.equals("RESB"))
				{
					int intLOCCTR = (Integer.parseInt(LOCCTR, 16))
							+ Integer.parseInt(operand); 
					LOCCTR = Integer.toHexString(intLOCCTR);
					i.add(null);
				}
				else if (mnemonic.equals("BYTE"))
				{
					int length;
					if (operand.startsWith("C"))
						length = (operand.length() - 3);
					else
						length = (operand.length() - 3) / 2;
					int intLOCCTR = (Integer.parseInt(LOCCTR, 16)) + length; 
					LOCCTR = Integer.toHexString(intLOCCTR);
					String tempString = "";
					if (operand.startsWith("C"))
					{
						char tempChar;
						for (int j = 2; j < operand.length() - 1; ++j)
						{
							tempChar = operand.charAt(j);
							int tempInt = (int) tempChar;
							tempString += Integer.toHexString(tempInt);

						}
					}
					else if (operand.startsWith("X"))
					{
						for (int j = 2; j < operand.length() - 1; ++j)
						{
							tempString += operand.charAt(j);
						}
					}
					i.add(4, tempString);

				}

				else if (mnemonic.equals("START"))
				{
					controlSection = label;
					estab.put(label, null);
				}

				else if (mnemonic.equals("EXTDEF"))
				{
					String[] splitOperand = operand.split(",");
					for (String j : splitOperand)
					{
						extdef.add(j);
					}

				}
				else if (mnemonic.equals("EXTREF"))
				{
					String[] splitOperand = operand.split(",");
					for (String j : splitOperand)
					{
						extref.add(j);
					}
				}

				tempop.add(i);
			}

		}
		FileWriter fw = new FileWriter("SYMTAB.DAT");
		for(ArrayList<String> j : tempop)
			fw.append(j.toString() + "\n");
		fw.close();
		fw = new FileWriter("EXTDEF.DAT");
			fw.append(extdef.toString());
		fw.close();
		fw = new FileWriter("EXTREF.DAT");
			fw.append(extref.toString());
		fw.close();
		System.out.println(tempop.toString());
	}
}
