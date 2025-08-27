from datetime import datetime
from database import db


class User(db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password = db.Column(db.String(255), nullable=False)
    avatar_url = db.Column(db.String(255), default='')
    avatar_type = db.Column(db.String(50), default='')
    created_at = db.Column(db.BigInteger, default=lambda: int(datetime.now().timestamp() * 1000))
    last_login = db.Column(db.BigInteger, default=lambda: int(datetime.now().timestamp() * 1000))

    # 关系定义
    posts = db.relationship('Post', backref='author', lazy=True)
    comments = db.relationship('Comment', backref='author', lazy=True)

    def to_dict(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'password': '',
            'avatarUrl': self.avatar_url,
            'avatarType': self.avatar_type,
            'createdAt': self.created_at,
            'lastLogin': self.last_login
        }

    def __repr__(self):
        return f'<User {self.username}>'


class Post(db.Model):
    __tablename__ = 'posts'

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    author_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    title = db.Column(db.String(200), nullable=False)
    content = db.Column(db.Text, nullable=False)
    category = db.Column(db.String(50), default='General')
    created_at = db.Column(db.BigInteger, default=lambda: int(datetime.now().timestamp() * 1000))
    updated_at = db.Column(db.BigInteger, default=lambda: int(datetime.now().timestamp() * 1000),
                           onupdate=lambda: int(datetime.now().timestamp() * 1000))

    # 关系定义
    comments = db.relationship('Comment', backref='post', lazy=True, cascade='all, delete-orphan')

    def to_dict(self):
        return {
            'id': self.id,
            'authorId': self.author_id,
            'title': self.title,
            'content': self.content,
            'category': self.category,
            'createdAt': self.created_at,
            'updatedAt': self.updated_at
        }

    def __repr__(self):
        return f'<Post {self.title}>'


class Comment(db.Model):
    __tablename__ = 'comments'

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    post_id = db.Column(db.Integer, db.ForeignKey('posts.id'), nullable=False)
    author_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    content = db.Column(db.Text, nullable=False)
    created_at = db.Column(db.BigInteger, default=lambda: int(datetime.now().timestamp() * 1000))

    def to_dict(self):
        return {
            'id': self.id,
            'postId': self.post_id,
            'authorId': self.author_id,
            'content': self.content,
            'createdAt': self.created_at
        }

    def __repr__(self):
        return f'<Comment {self.id} for Post {self.post_id}>'