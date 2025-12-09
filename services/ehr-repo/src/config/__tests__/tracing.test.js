import { getCurrentSpanAttributes, setCurrentSpanAttributes, startRequest } from '../tracing';

describe('tracing config', () => {
  it('should let us store and retrieve attributes related to the request', () => {
    startRequest(() => {
      setCurrentSpanAttributes({ weather: 'cloudy' });
      const attributes = getCurrentSpanAttributes();
      expect(attributes.weather).toBe('cloudy');
    });
  });

  it('should not let us retrieve stored attributes after the request has ended', () => {
    startRequest(() => {
      setCurrentSpanAttributes({ city: 'London' });
    });
    const attributes = getCurrentSpanAttributes();
    expect(attributes).toBe(undefined);
  });

  it('should support multiple independent requests', () => {
    startRequest(() => {
      setCurrentSpanAttributes({ river: 'Thames' });

      expect(getCurrentSpanAttributes()).toStrictEqual({ river: 'Thames' });
    });
    startRequest(() => {
      setCurrentSpanAttributes({ mountain: 'Snowden' });

      expect(getCurrentSpanAttributes()).toStrictEqual({ mountain: 'Snowden' });
    });
  });

  it('should let us store and retrieve multiple attributes', () => {
    startRequest(() => {
      setCurrentSpanAttributes({ colour: 'green', shape: 'oval' });
      const attributes = getCurrentSpanAttributes();
      expect(attributes).toStrictEqual({ colour: 'green', shape: 'oval' });
    });
  });

  it('should let us retrieve the merged set of attributes from multiple calls', () => {
    startRequest(() => {
      setCurrentSpanAttributes({ floor: 'carpet', paint: 'white' });
      setCurrentSpanAttributes({ paint: 'green', door: 'wood' });
      const attributes = getCurrentSpanAttributes();
      expect(attributes).toStrictEqual({ floor: 'carpet', paint: 'green', door: 'wood' });
    });
  });
});
