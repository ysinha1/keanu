from keanu import Model, Uniform, Gaussian

def model():

    with Model() as m:
    	m.temperature = Uniform(0., 100.)
    	m.thermometer_one = Gaussian(m.temperature, 1.0)
    	m.thermometer_two = Gaussian(m.temperature, 1.0)

    return m