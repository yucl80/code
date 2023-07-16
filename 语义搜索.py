from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModel
from annoy import AnnoyIndex
import torch
import numpy as np

tokenizer = AutoTokenizer.from_pretrained('shibing624/text2vec-base-multilingual')
model = AutoModel.from_pretrained('shibing624/text2vec-base-multilingual')

app = FastAPI()

class Sentence(BaseModel):
    sentence: str

def get_sentence_embedding(sentence):
    inputs = tokenizer(sentence, return_tensors='pt')
    outputs = model(**inputs)
    return outputs.last_hidden_state.mean(dim=1).detach().numpy()

index = AnnoyIndex(768, 'angular')  # 768 is the dimension of the vectors
sentences = []
sentence_vectors = []

@app.post("/add")
async def add_sentence(item: Sentence):
    sentence = item.sentence
    vector = get_sentence_embedding(sentence)
    sentences.append(sentence)
    sentence_vectors.append(vector)
    index.add_item(len(sentences) - 1, vector)
    index.build(10)  # 10 is the number of trees for the index. More trees give higher precision.
    return {"message": "Sentence added successfully."}

@app.post("/search")
async def search_sentence(item: Sentence):
    search_sentence = item.sentence
    search_embedding = get_sentence_embedding(search_sentence)
    top_5 = index.get_nns_by_vector(search_embedding, 5)
    return ', '.join([sentences[i] for i in top_5][::-1])

uvicorn main:app --reload
curl -X POST -H "Content-Type: application/json" -d '{"sentence":"your sentence"}' http://localhost:8000/add
