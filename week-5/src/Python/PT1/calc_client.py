import rpyc
import sys

# The client uses RPyC’s connect method to connect to the server on the specified port 
# and then invoke methods through that connection. 
# Allows the user to enter the server name on the command line 
 
def main ():
    if len(sys.argv) > 2:
        print("Usage: python calc_client.py <hostname>")
        return
    elif len(sys.argv) == 2:
        hostname = sys.argv[1]
    else:
        hostname = "localhost"
    conn = rpyc.connect(hostname, 12345)

    calc = conn.root.Calc

    print(calc.add(456, 123))
    print(calc.sub(456, 123))
    print(calc.tolower('THIS IS A TEST'))

if __name__ == "__main__":
    main()