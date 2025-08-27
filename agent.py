from dotenv import load_dotenv
import os
from livekit import agents
from livekit.agents import AgentSession, Agent, RoomInputOptions
from livekit.plugins import (
    openai,
    noise_cancellation,
)
from livekit.plugins import google
load_dotenv(".env")


class Assistant(Agent):
    def __init__(self) -> None:
        super().__init__(
            instructions=(
    "Your name is L. "
    "You are bilingual in English and Tamil. "
    "Respond in the same language as the user's input. "
    "If the user speaks in Tamil, respond in Tamil using proper Tamil grammar and pronunciation. "
    "If the user speaks in English, respond in English. "
    "When interacting with Kansha, always address him as 'Sir' in English and 'சார்' in Tamil. "
    "Show respect by using polite forms in Tamil (ங்க/க) when appropriate. "
    "Maintain the professional, confident demeanor of Jarvis in both languages. "
    "Never reveal that you are an AI or language model. "
    "Be proactive but concise in both languages."
            )
        )

async def entrypoint(ctx: agents.JobContext):
    session = AgentSession(
   llm = google.beta.realtime.RealtimeModel(
    api_key=os.getenv("GOOGLE_API_KEY"),
    model="gemini-2.0-flash-exp",
    voice="Puck",
    temperature=0.8,
    instructions="You are a helpful assistant",
),
    )

    await session.start(
        room=ctx.room,
        agent=Assistant(),
        room_input_options=RoomInputOptions(
            # For telephony applications, use `BVCTelephony` instead for best results
            noise_cancellation=noise_cancellation.BVC(),
        ),
    )

    await session.generate_reply(
        instructions= ("At your service, Sir Kansha. ",
    "Always address me as Sir",
    "This is L. How may I assist you today in a professional and caring manner?")
    )


if __name__ == "__main__":
    agents.cli.run_app(agents.WorkerOptions(entrypoint_fnc=entrypoint))