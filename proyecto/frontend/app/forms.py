from flask_wtf import FlaskForm
from wtforms import StringField, PasswordField, SubmitField, TextAreaField
from wtforms.validators import DataRequired, EqualTo, Length, Email

class RegisterForm(FlaskForm):
    username = StringField('Username', validators=[DataRequired(), Length(min=3, max=25)])
    email = StringField('Email', validators=[DataRequired(), Email()])
    password = PasswordField('Password', validators=[DataRequired(), Length(min=6)])
    confirm = PasswordField('Repeat Password', validators=[
        DataRequired(), EqualTo('password', message='Passwords must match.')
    ])
    submit = SubmitField('Register')

class LoginForm(FlaskForm):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    submit = SubmitField('Login')

class NewDialogueForm(FlaskForm):
    dialogue_id = StringField('Conversation Name', validators=[DataRequired(), Length(min=1, max=50)])
    submit = SubmitField('Start Conversation')

class PromptForm(FlaskForm):
    prompt = TextAreaField('Your message', validators=[DataRequired(), Length(min=1)])
    submit = SubmitField('Send')
