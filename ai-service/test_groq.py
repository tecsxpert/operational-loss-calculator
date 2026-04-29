import os
from dotenv import load_dotenv
from groq import Groq

def test_groq_connection():
    # Load environment variables from the .env file in the project root
    # Since test_groq.py is in ai-service/, we need to look in the parent directory
    # 'load_dotenv' typically finds the nearest .env automatically, but we can be explicit if needed.
    # Actually load_dotenv() from project root will work if we run it from root. We will use find_dotenv to be reliable.
    from dotenv import find_dotenv
    load_dotenv(find_dotenv())

    # Initialize the Groq client
    # The client automatically picks up the GROQ_API_KEY environment variable
    client = Groq()

    print("Sending 'Hello World' request to Groq (llama-3.3-70b-versatile)...")
    try:
        # Make a simple "Hello World" request
        chat_completion = client.chat.completions.create(
            messages=[
                {
                    "role": "user",
                    "content": "Hello World. Reply strictly with 'Hello World'.",
                }
            ],
            model="llama-3.3-70b-versatile",
        )
        
        # Print the response to confirm connectivity
        print("Response received:")
        print(chat_completion.choices[0].message.content)
    except Exception as e:
        print(f"Failed to connect: {e}")

if __name__ == "__main__":
    test_groq_connection()
