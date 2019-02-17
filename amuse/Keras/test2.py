from keras.models import Model
from keras.layers import Input, Dense
import arff

a = Input(shape=(32,))
b = Dense(32)(a)
model = Model(inputs=a, outputs=b)

#We load the arff file
dataarff = arff.load(open('../Categories/Test.arff'),'rb')
#print(data)

#We retrieve the data as a table
data = dataarff.get("data")
print(data[0][1])